package com.retailable.supermarket.inventory;

import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class InventoryService {
    private final InventoryMapper mapper; private final AuditService audit;
    public InventoryService(InventoryMapper mapper,AuditService audit){this.mapper=mapper;this.audit=audit;}

    public PageResult<Map<String,Object>> page(String keyword,boolean lowOnly,int page,int size){int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));return PageResult.of(mapper.page(keyword,lowOnly,(p-1)*s,s),mapper.count(keyword,lowOnly),p,s);}
    public PageResult<Map<String,Object>> movements(Long productId,String type,int page,int size){int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));return PageResult.of(mapper.movements(productId,type,(p-1)*s,s),mapper.movementCount(productId,type),p,s);}

    @Transactional
    public InventoryRow apply(Long productId,int currentDelta,int reservedDelta,String type,String ref,Long operatorId,String remark){
        InventoryRow before=mapper.lock(productId);
        if(before==null)throw BusinessException.notFound("商品库存记录不存在");
        if(mapper.change(productId,currentDelta,reservedDelta)!=1){
            if(reservedDelta>0)throw BusinessException.conflict("库存不足，无法预占");
            throw BusinessException.conflict("库存变更会造成负库存或预占异常");
        }
        int afterQty=before.getCurrentQty()+currentDelta,afterReserved=before.getReservedQty()+reservedDelta;
        mapper.movement(productId,currentDelta,reservedDelta,before.getCurrentQty(),afterQty,before.getReservedQty(),afterReserved,type,ref,operatorId,remark);
        InventoryRow result=new InventoryRow();result.setProductId(productId);result.setCurrentQty(afterQty);result.setReservedQty(afterReserved);return result;
    }

    @Transactional
    public InventoryRow adjust(Long productId,int targetQty,Long operatorId,String reason){
        if(reason==null||reason.isBlank())throw BusinessException.badRequest("盘点原因不能为空");
        InventoryRow before=mapper.lock(productId);
        if(before==null)throw BusinessException.notFound("商品库存记录不存在");
        if(targetQty<before.getReservedQty())throw BusinessException.conflict("盘点后库存不能低于已预占库存 "+before.getReservedQty());
        int delta=targetQty-before.getCurrentQty();
        InventoryRow result=apply(productId,delta,0,"STOCKTAKE","STK-"+UUID.randomUUID(),operatorId,reason);
        audit.record("INVENTORY_ADJUST","PRODUCT",productId,"盘点调整 "+delta+"，原因："+reason);
        return result;
    }

    @Transactional
    public InventoryRow manualMovement(Long productId,int quantity,boolean inbound,Long operatorId,String reason){
        if(quantity<=0)throw BusinessException.badRequest("数量必须大于0");
        if(reason==null||reason.isBlank())throw BusinessException.badRequest("变更原因不能为空");
        int delta=inbound?quantity:-quantity;
        InventoryRow result=apply(productId,delta,0,inbound?"MANUAL_IN":"MANUAL_OUT","MAN-"+UUID.randomUUID(),operatorId,reason);
        audit.record(inbound?"INVENTORY_IN":"INVENTORY_OUT","PRODUCT",productId,(inbound?"手工入库 ":"手工出库 ")+quantity);
        return result;
    }
}

