package com.retailable.supermarket.purchase;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface PurchaseMapper {
    @Insert("INSERT INTO purchase_order(order_no,supplier_id,status,total_amount,remark,created_by) VALUES(#{orderNo},#{supplierId},'DRAFT',#{totalAmount},#{remark},#{createdBy})")
    @Options(useGeneratedKeys=true,keyProperty="id") int insertOrder(PurchaseOrderRow row);
    @Insert("INSERT INTO purchase_item(purchase_order_id,product_id,product_code,product_name,specification,unit,quantity,unit_price,amount) VALUES(#{orderId},#{productId},#{productCode},#{productName},#{specification},#{unit},#{quantity},#{unitPrice},#{amount})")
    @Options(useGeneratedKeys=true,keyProperty="id") int insertItem(PurchaseItemRow row);
    @Select("SELECT po.*,s.name supplier_name,u.display_name creator_name FROM purchase_order po JOIN supplier s ON s.id=po.supplier_id JOIN sys_user u ON u.id=po.created_by WHERE po.id=#{id}") Map<String,Object> findById(Long id);
    @Select("SELECT * FROM purchase_order WHERE id=#{id} FOR UPDATE") Map<String,Object> lock(Long id);
    @Select("SELECT * FROM purchase_item WHERE purchase_order_id=#{orderId} ORDER BY id") List<Map<String,Object>> items(Long orderId);
    @Update("UPDATE purchase_order SET status='WAREHOUSED',confirmed_by=#{operatorId},confirmed_at=NOW(3) WHERE id=#{id} AND status='DRAFT'") int confirm(@Param("id")Long id,@Param("operatorId")Long operatorId);
    @Update("UPDATE purchase_order SET status='CANCELLED' WHERE id=#{id} AND status='DRAFT'") int cancel(Long id);
    @Select("<script>SELECT po.*,s.name supplier_name,u.display_name creator_name FROM purchase_order po JOIN supplier s ON s.id=po.supplier_id JOIN sys_user u ON u.id=po.created_by <where><if test='status != null and status != &quot;&quot;'>AND po.status=#{status}</if><if test='keyword != null and keyword != &quot;&quot;'>AND (po.order_no LIKE CONCAT('%',#{keyword},'%') OR s.name LIKE CONCAT('%',#{keyword},'%'))</if></where> ORDER BY po.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> page(@Param("keyword")String keyword,@Param("status")String status,@Param("offset")int offset,@Param("pageSize")int pageSize);
    @Select("<script>SELECT COUNT(*) FROM purchase_order po JOIN supplier s ON s.id=po.supplier_id <where><if test='status != null and status != &quot;&quot;'>AND po.status=#{status}</if><if test='keyword != null and keyword != &quot;&quot;'>AND (po.order_no LIKE CONCAT('%',#{keyword},'%') OR s.name LIKE CONCAT('%',#{keyword},'%'))</if></where></script>") long count(@Param("keyword")String keyword,@Param("status")String status);

    class PurchaseOrderRow {
        public Long id,supplierId,createdBy; public String orderNo,remark; public BigDecimal totalAmount;
        public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getSupplierId(){return supplierId;} public Long getCreatedBy(){return createdBy;} public String getOrderNo(){return orderNo;} public String getRemark(){return remark;} public BigDecimal getTotalAmount(){return totalAmount;}
    }
    class PurchaseItemRow {
        public Long id,orderId,productId; public String productCode,productName,specification,unit; public int quantity; public BigDecimal unitPrice,amount;
        public Long getId(){return id;}public void setId(Long id){this.id=id;}public Long getOrderId(){return orderId;}public Long getProductId(){return productId;}public String getProductCode(){return productCode;}public String getProductName(){return productName;}public String getSpecification(){return specification;}public String getUnit(){return unit;}public int getQuantity(){return quantity;}public BigDecimal getUnitPrice(){return unitPrice;}public BigDecimal getAmount(){return amount;}
    }
}

