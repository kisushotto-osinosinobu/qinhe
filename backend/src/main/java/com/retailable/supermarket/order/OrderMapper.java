package com.retailable.supermarket.order;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO sale_order(order_no,idempotency_key,channel,member_id,cashier_id,status,total_amount) VALUES(#{orderNo},#{idempotencyKey},#{channel},#{memberId},#{cashierId},'PENDING_PAYMENT',#{totalAmount})")
    @Options(useGeneratedKeys=true,keyProperty="id") int insertOrder(SaleOrderRow row);
    @Insert("INSERT INTO sale_item(sale_order_id,product_id,product_code,barcode,product_name,specification,unit,quantity,unit_price,amount) VALUES(#{orderId},#{productId},#{productCode},#{barcode},#{productName},#{specification},#{unit},#{quantity},#{unitPrice},#{amount})")
    @Options(useGeneratedKeys=true,keyProperty="id") int insertItem(SaleItemRow row);
    @Select("SELECT so.*,m.display_name member_name,c.display_name cashier_name FROM sale_order so LEFT JOIN sys_user m ON m.id=so.member_id LEFT JOIN sys_user c ON c.id=so.cashier_id WHERE so.id=#{id}") Map<String,Object> findById(Long id);
    @Select("SELECT * FROM sale_order WHERE idempotency_key=#{key}") Map<String,Object> findByIdempotency(String key);
    @Select("SELECT * FROM sale_order WHERE id=#{id} FOR UPDATE") Map<String,Object> lock(Long id);
    @Select("SELECT * FROM sale_item WHERE sale_order_id=#{orderId} ORDER BY id") List<Map<String,Object>> items(Long orderId);
    @Select("SELECT * FROM sale_item WHERE id=#{itemId} AND sale_order_id=#{orderId} FOR UPDATE") Map<String,Object> lockItem(@Param("itemId")Long itemId,@Param("orderId")Long orderId);
    @Update("UPDATE sale_order SET status='PAID',paid_amount=total_amount,payment_method=#{method},paid_at=NOW(3),cashier_id=#{cashierId} WHERE id=#{id} AND status='PENDING_PAYMENT'") int pay(@Param("id")Long id,@Param("method")String method,@Param("cashierId")Long cashierId);
    @Update("UPDATE sale_order SET status='CANCELLED',cancelled_at=NOW(3) WHERE id=#{id} AND status='PENDING_PAYMENT'") int cancel(Long id);
    @Select("SELECT * FROM sale_return WHERE idempotency_key=#{key}") Map<String,Object> findReturnByKey(String key);
    @Insert("INSERT INTO sale_return(return_no,sale_order_id,idempotency_key,amount,reason,operator_id) VALUES(#{returnNo},#{orderId},#{idempotencyKey},#{amount},#{reason},#{operatorId})")
    @Options(useGeneratedKeys=true,keyProperty="id") int insertReturn(ReturnRow row);
    @Insert("INSERT INTO sale_return_item(sale_return_id,sale_item_id,product_id,quantity,unit_price,amount) VALUES(#{returnId},#{saleItemId},#{productId},#{quantity},#{unitPrice},#{amount})") int insertReturnItem(ReturnItemRow row);
    @Update("UPDATE sale_item SET returned_qty=returned_qty+#{qty} WHERE id=#{itemId} AND returned_qty+#{qty}<=quantity") int addReturned(@Param("itemId")Long itemId,@Param("qty")int qty);
    @Select("SELECT COUNT(*) FROM sale_item WHERE sale_order_id=#{orderId} AND returned_qty<quantity") int unreturnedLineCount(Long orderId);
    @Update("UPDATE sale_order SET refund_amount=refund_amount+#{amount},status=#{status} WHERE id=#{id}") int addRefund(@Param("id")Long id,@Param("amount")BigDecimal amount,@Param("status")String status);
    @Select("<script>SELECT so.*,m.display_name member_name,c.display_name cashier_name FROM sale_order so LEFT JOIN sys_user m ON m.id=so.member_id LEFT JOIN sys_user c ON c.id=so.cashier_id <where><if test='memberId != null'>AND so.member_id=#{memberId}</if><if test='status != null and status != &quot;&quot;'>AND so.status=#{status}</if><if test='keyword != null and keyword != &quot;&quot;'>AND so.order_no LIKE CONCAT('%',#{keyword},'%')</if></where> ORDER BY so.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> page(@Param("memberId")Long memberId,@Param("keyword")String keyword,@Param("status")String status,@Param("offset")int offset,@Param("pageSize")int pageSize);
    @Select("<script>SELECT COUNT(*) FROM sale_order so <where><if test='memberId != null'>AND so.member_id=#{memberId}</if><if test='status != null and status != &quot;&quot;'>AND so.status=#{status}</if><if test='keyword != null and keyword != &quot;&quot;'>AND so.order_no LIKE CONCAT('%',#{keyword},'%')</if></where></script>") long count(@Param("memberId")Long memberId,@Param("keyword")String keyword,@Param("status")String status);

    class SaleOrderRow {public Long id,memberId,cashierId;public String orderNo,idempotencyKey,channel;public BigDecimal totalAmount;public Long getId(){return id;}public void setId(Long id){this.id=id;}public Long getMemberId(){return memberId;}public Long getCashierId(){return cashierId;}public String getOrderNo(){return orderNo;}public String getIdempotencyKey(){return idempotencyKey;}public String getChannel(){return channel;}public BigDecimal getTotalAmount(){return totalAmount;}}
    class SaleItemRow {public Long id,orderId,productId;public String productCode,barcode,productName,specification,unit;public int quantity;public BigDecimal unitPrice,amount;public Long getId(){return id;}public void setId(Long id){this.id=id;}public Long getOrderId(){return orderId;}public Long getProductId(){return productId;}public String getProductCode(){return productCode;}public String getBarcode(){return barcode;}public String getProductName(){return productName;}public String getSpecification(){return specification;}public String getUnit(){return unit;}public int getQuantity(){return quantity;}public BigDecimal getUnitPrice(){return unitPrice;}public BigDecimal getAmount(){return amount;}}
    class ReturnRow {public Long id,orderId,operatorId;public String returnNo,idempotencyKey,reason;public BigDecimal amount;public Long getId(){return id;}public void setId(Long id){this.id=id;}public Long getOrderId(){return orderId;}public Long getOperatorId(){return operatorId;}public String getReturnNo(){return returnNo;}public String getIdempotencyKey(){return idempotencyKey;}public String getReason(){return reason;}public BigDecimal getAmount(){return amount;}}
    class ReturnItemRow {public Long returnId,saleItemId,productId;public int quantity;public BigDecimal unitPrice,amount;public Long getReturnId(){return returnId;}public Long getSaleItemId(){return saleItemId;}public Long getProductId(){return productId;}public int getQuantity(){return quantity;}public BigDecimal getUnitPrice(){return unitPrice;}public BigDecimal getAmount(){return amount;}}
}

