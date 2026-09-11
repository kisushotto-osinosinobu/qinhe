package com.retailable.supermarket.supplier;

import com.retailable.supermarket.audit.AuditService;
import com.retailable.supermarket.common.BusinessException;
import com.retailable.supermarket.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.retailable.supermarket.supplier.SupplierDtos.SupplierRequest;

@Service
public class SupplierService {
    private final SupplierMapper mapper; private final AuditService audit;
    public SupplierService(SupplierMapper mapper,AuditService audit){this.mapper=mapper;this.audit=audit;}
    public PageResult<Map<String,Object>> page(String keyword,int page,int size){int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));return PageResult.of(mapper.page(keyword,(p-1)*s,s),mapper.count(keyword),p,s);}
    public List<Map<String,Object>> enabled(){return mapper.enabled();}
    @Transactional public Map<String,Object> create(SupplierRequest r){var row=row(null,r);mapper.insert(row);audit.record("SUPPLIER_CREATE","SUPPLIER",row.id,row.name);return mapper.findById(row.id);}
    @Transactional public Map<String,Object> update(Long id,SupplierRequest r){require(id);var row=row(id,r);mapper.update(row);audit.record("SUPPLIER_UPDATE","SUPPLIER",id,row.name);return mapper.findById(id);}
    @Transactional public void delete(Long id){require(id);if(mapper.purchaseCount(id)>0)throw BusinessException.conflict("供应商已有采购记录，不能删除；可改为停用");mapper.delete(id);audit.record("SUPPLIER_DELETE","SUPPLIER",id,"删除未使用供应商");}
    private void require(Long id){if(mapper.findById(id)==null)throw BusinessException.notFound("供应商不存在");}
    private SupplierMapper.SupplierRow row(Long id,SupplierRequest r){var x=new SupplierMapper.SupplierRow();x.id=id;x.code=r.code();x.name=r.name();x.contactName=r.contactName();x.phone=r.phone();x.email=r.email();x.address=r.address();x.status=r.status();x.remark=r.remark();return x;}
}

