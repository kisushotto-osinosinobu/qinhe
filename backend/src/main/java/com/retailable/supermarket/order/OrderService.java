package com.retailable.supermarket.order;

import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.catalog.ProductMapper;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.common.PageResult;
import com.retailable.supermarket.inventory.InventoryService;
import com.retailable.supermarket.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.retailable.supermarket.order.OrderDtos.*;

@Service
public class OrderService {
    private final OrderMapper mapper;private final ProductMapper productMapper;private final InventoryService inventory;private final AuditService audit;
    public OrderService(OrderMapper mapper,ProductMapper productMapper,InventoryService inventory,AuditService audit){this.mapper=mapper;this.productMapper=productMapper;this.inventory=inventory;this.audit=audit;}

    public PageResult<Map<String,Object>> page(UserPrincipal principal,String keyword,String status,int page,int size){Long memberId="MEMBER".equals(principal.role())?principal.id():null;int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));return PageResult.of(mapper.page(memberId,keyword,status,(p-1)*s,s),mapper.count(memberId,keyword,status),p,s);}
    public Map<String,Object> detail(Long id,UserPrincipal principal){Map<String,Object> order=mapper.findById(id);authorize(order,principal);var result=new LinkedHashMap<>(order);result.put("items",mapper.items(id));return result;}

    @Transactional
    public Map<String,Object> create(CreateOrderRequest r,UserPrincipal principal){
        Map<String,Object> existing=mapper.findByIdempotency(r.idempotencyKey());
        if(existing!=null)return detail(((Number)existing.get("id")).longValue(),principal);
        Set<Long> seen=new HashSet<>();List<OrderMapper.SaleItemRow> rows=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;
        for(OrderLine line:r.items()){
            if(!seen.add(line.productId()))throw BusinessException.badRequest("同一商品不能重复添加");
            Map<String,Object> product=productMapper.findById(line.productId());
            if(product==null||!"ON_SALE".equals(product.get("status")))throw BusinessException.badRequest("商品不存在或已下架: "+line.productId());
            var item=new OrderMapper.SaleItemRow();item.productId=line.productId();item.productCode=(String)product.get("code");item.barcode=(String)product.get("barcode");item.productName=(String)product.get("name");item.specification=(String)product.get("specification");item.unit=(String)product.get("unit");item.quantity=line.quantity();item.unitPrice=(BigDecimal)product.get("salePrice");item.amount=item.unitPrice.multiply(BigDecimal.valueOf(line.quantity()));rows.add(item);total=total.add(item.amount);
        }
        boolean member="MEMBER".equals(principal.role());
        var order=new OrderMapper.SaleOrderRow();order.orderNo=number("SO");order.idempotencyKey=r.idempotencyKey();order.channel=member?"MINIAPP":"WEB_POS";order.memberId=member?principal.id():null;order.cashierId=member?null:principal.id();order.totalAmount=total;mapper.insertOrder(order);
        for(var item:rows){item.orderId=order.id;mapper.insertItem(item);inventory.apply(item.productId,0,item.quantity,"SALE_RESERVE",order.orderNo+":"+item.id,principal.id(),"订单预占 "+order.orderNo);}
        audit.record("SALE_ORDER_CREATE","SALE_ORDER",order.id,order.orderNo);
        if(!member&&Boolean.TRUE.equals(r.autoPay()))payInternal(order.id,r.paymentMethod()==null?"CASH":r.paymentMethod(),principal);
        return detail(order.id,principal);
    }

    @Transactional
    public Map<String,Object> pay(Long id,PayRequest r,UserPrincipal principal){return payInternal(id,r.paymentMethod(),principal);}

    private Map<String,Object> payInternal(Long id,String method,UserPrincipal principal){
        Map<String,Object> locked=mapper.lock(id);if(locked==null)throw BusinessException.notFound("销售单不存在");String status=(String)locked.get("status");
        if("PAID".equals(status)||"PARTIALLY_RETURNED".equals(status)||"RETURNED".equals(status))return detail(id,principal);
        if(!"PENDING_PAYMENT".equals(status))throw BusinessException.conflict("当前订单状态不能收款");
        if(mapper.pay(id,method,principal.id())!=1)throw BusinessException.conflict("订单状态已变化");
        String no=(String)locked.get("orderNo");for(Map<String,Object> item:mapper.items(id)){Long pid=num(item,"productId"),itemId=num(item,"id");int qty=((Number)item.get("quantity")).intValue();inventory.apply(pid,-qty,-qty,"SALE_PAID",no+":"+itemId,principal.id(),"模拟收款出库 "+no);}
        audit.record("SALE_PAY","SALE_ORDER",id,no+" / "+method);
        return detail(id,principal);
    }

    @Transactional
    public Map<String,Object> cancel(Long id,UserPrincipal principal){
        Map<String,Object> locked=mapper.lock(id);authorize(locked,principal);String status=(String)locked.get("status");if("CANCELLED".equals(status))return detail(id,principal);if(!"PENDING_PAYMENT".equals(status))throw BusinessException.conflict("仅待付款订单可取消");
        if(mapper.cancel(id)!=1)throw BusinessException.conflict("订单状态已变化");String no=(String)locked.get("orderNo");for(Map<String,Object> item:mapper.items(id)){Long pid=num(item,"productId"),itemId=num(item,"id");int qty=((Number)item.get("quantity")).intValue();inventory.apply(pid,0,-qty,"SALE_CANCEL",no+":"+itemId,principal.id(),"取消释放预占 "+no);}audit.record("SALE_CANCEL","SALE_ORDER",id,no);return detail(id,principal);
    }

    @Transactional
    public Map<String,Object> returnGoods(Long id,ReturnRequest r,UserPrincipal principal){
        Map<String,Object> prior=mapper.findReturnByKey(r.idempotencyKey());
        if(prior!=null){if(!Objects.equals(num(prior,"saleOrderId"),id))throw BusinessException.conflict("退货幂等键已被其他订单使用");return detail(id,principal);}
        Map<String,Object> locked=mapper.lock(id);if(locked==null)throw BusinessException.notFound("销售单不存在");String status=(String)locked.get("status");if(!List.of("PAID","PARTIALLY_RETURNED").contains(status))throw BusinessException.conflict("仅已付款订单可退货");
        Set<Long> seen=new HashSet<>();List<OrderMapper.ReturnItemRow> rows=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;
        for(ReturnLine line:r.items()){
            if(!seen.add(line.saleItemId()))throw BusinessException.badRequest("退货明细不能重复");Map<String,Object> item=mapper.lockItem(line.saleItemId(),id);if(item==null)throw BusinessException.notFound("销售明细不存在");int sold=((Number)item.get("quantity")).intValue(),returned=((Number)item.get("returnedQty")).intValue();if(returned+line.quantity()>sold)throw BusinessException.conflict("累计退货数量不能超过原销售数量");
            var x=new OrderMapper.ReturnItemRow();x.saleItemId=line.saleItemId();x.productId=num(item,"productId");x.quantity=line.quantity();x.unitPrice=(BigDecimal)item.get("unitPrice");x.amount=x.unitPrice.multiply(BigDecimal.valueOf(x.quantity));rows.add(x);total=total.add(x.amount);
        }
        var ret=new OrderMapper.ReturnRow();ret.returnNo=number("RT");ret.orderId=id;ret.idempotencyKey=r.idempotencyKey();ret.amount=total;ret.reason=r.reason();ret.operatorId=principal.id();mapper.insertReturn(ret);
        for(var x:rows){x.returnId=ret.id;if(mapper.addReturned(x.saleItemId,x.quantity)!=1)throw BusinessException.conflict("退货数量已被其他操作修改");mapper.insertReturnItem(x);inventory.apply(x.productId,x.quantity,0,"SALE_RETURN",ret.returnNo+":"+x.saleItemId,principal.id(),"退货回补 "+ret.returnNo);}
        String next=mapper.unreturnedLineCount(id)==0?"RETURNED":"PARTIALLY_RETURNED";mapper.addRefund(id,total,next);audit.record("SALE_RETURN","SALE_ORDER",id,ret.returnNo+" / "+total);return detail(id,principal);
    }

    private void authorize(Map<String,Object> order,UserPrincipal principal){if(order==null)throw BusinessException.notFound("销售单不存在");if("MEMBER".equals(principal.role())){Object member=order.get("memberId");if(member==null||((Number)member).longValue()!=principal.id())throw BusinessException.forbidden("不能访问他人订单");}}
    private Long num(Map<String,Object> map,String key){return ((Number)map.get(key)).longValue();}
    private String number(String prefix){return prefix+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+String.format("%04d",java.util.concurrent.ThreadLocalRandom.current().nextInt(10000));}
}
