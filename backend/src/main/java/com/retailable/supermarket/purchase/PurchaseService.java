package com.retailable.supermarket.purchase;

import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.catalog.ProductMapper;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.common.PageResult;
import com.retailable.supermarket.inventory.InventoryService;
import com.retailable.supermarket.supplier.SupplierMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.retailable.supermarket.purchase.PurchaseDtos.*;

@Service
public class PurchaseService {
    private final PurchaseMapper mapper; private final ProductMapper productMapper; private final SupplierMapper supplierMapper;
    private final InventoryService inventory; private final AuditService audit;
    public PurchaseService(PurchaseMapper mapper,ProductMapper productMapper,SupplierMapper supplierMapper,InventoryService inventory,AuditService audit){this.mapper=mapper;this.productMapper=productMapper;this.supplierMapper=supplierMapper;this.inventory=inventory;this.audit=audit;}
    public PageResult<Map<String,Object>> page(String keyword,String status,int page,int size){int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));return PageResult.of(mapper.page(keyword,status,(p-1)*s,s),mapper.count(keyword,status),p,s);}
    public Map<String,Object> detail(Long id){Map<String,Object> order=mapper.findById(id);if(order==null)throw BusinessException.notFound("采购单不存在");var result=new LinkedHashMap<>(order);result.put("items",mapper.items(id));return result;}

    @Transactional
    public Map<String,Object> create(PurchaseRequest r,Long userId){
        Map<String,Object> supplier=supplierMapper.findById(r.supplierId());
        if(supplier==null||!"ENABLED".equals(supplier.get("status")))throw BusinessException.badRequest("供应商不存在或已停用");
        Set<Long> seen=new HashSet<>();List<PurchaseMapper.PurchaseItemRow> rows=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;
        for(PurchaseLine line:r.items()){
            if(!seen.add(line.productId()))throw BusinessException.badRequest("同一商品不能重复添加");
            Map<String,Object> product=productMapper.findById(line.productId());if(product==null)throw BusinessException.notFound("商品不存在: "+line.productId());
            var item=new PurchaseMapper.PurchaseItemRow();item.productId=line.productId();item.productCode=(String)product.get("code");item.productName=(String)product.get("name");item.specification=(String)product.get("specification");item.unit=(String)product.get("unit");item.quantity=line.quantity();item.unitPrice=line.unitPrice();item.amount=line.unitPrice().multiply(BigDecimal.valueOf(line.quantity()));rows.add(item);total=total.add(item.amount);
        }
        var order=new PurchaseMapper.PurchaseOrderRow();order.orderNo=number("PO");order.supplierId=r.supplierId();order.totalAmount=total;order.remark=r.remark();order.createdBy=userId;mapper.insertOrder(order);
        for(var item:rows){item.orderId=order.id;mapper.insertItem(item);}
        audit.record("PURCHASE_CREATE","PURCHASE_ORDER",order.id,order.orderNo);
        return detail(order.id);
    }

    @Transactional
    public Map<String,Object> confirm(Long id,Long userId){
        Map<String,Object> locked=mapper.lock(id);if(locked==null)throw BusinessException.notFound("采购单不存在");
        String status=(String)locked.get("status");if("WAREHOUSED".equals(status))return detail(id);if(!"DRAFT".equals(status))throw BusinessException.conflict("仅草稿采购单可确认入库");
        if(mapper.confirm(id,userId)!=1)throw BusinessException.conflict("采购单状态已变化");
        String no=(String)locked.get("orderNo");
        for(Map<String,Object> item:mapper.items(id)){
            Long productId=((Number)item.get("productId")).longValue();int qty=((Number)item.get("quantity")).intValue();Long itemId=((Number)item.get("id")).longValue();
            inventory.apply(productId,qty,0,"PURCHASE_IN",no+":"+itemId,userId,"采购入库 "+no);
        }
        audit.record("PURCHASE_WAREHOUSE","PURCHASE_ORDER",id,no);
        return detail(id);
    }

    @Transactional public Map<String,Object> cancel(Long id){Map<String,Object> locked=mapper.lock(id);if(locked==null)throw BusinessException.notFound("采购单不存在");if("CANCELLED".equals(locked.get("status")))return detail(id);if(mapper.cancel(id)!=1)throw BusinessException.conflict("已入库采购单不能取消");audit.record("PURCHASE_CANCEL","PURCHASE_ORDER",id,(String)locked.get("orderNo"));return detail(id);}
    private String number(String prefix){return prefix+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+String.format("%04d",new Random().nextInt(10000));}
}

