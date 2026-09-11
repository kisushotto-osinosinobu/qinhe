package com.retailable.supermarket.inventory;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface InventoryMapper {
    @Select("SELECT * FROM inventory WHERE product_id=#{productId} FOR UPDATE") InventoryRow lock(Long productId);
    @Update("UPDATE inventory SET current_qty=current_qty+#{currentDelta},reserved_qty=reserved_qty+#{reservedDelta},version=version+1 " +
            "WHERE product_id=#{productId} AND current_qty+#{currentDelta}>=0 AND reserved_qty+#{reservedDelta}>=0 " +
            "AND reserved_qty+#{reservedDelta}<=current_qty+#{currentDelta}")
    int change(@Param("productId")Long productId,@Param("currentDelta")int currentDelta,@Param("reservedDelta")int reservedDelta);
    @Insert("INSERT INTO inventory_movement(product_id,change_qty,change_reserved_qty,before_qty,after_qty,before_reserved_qty,after_reserved_qty,business_type,business_ref,operator_id,remark) " +
            "VALUES(#{productId},#{currentDelta},#{reservedDelta},#{beforeQty},#{afterQty},#{beforeReserved},#{afterReserved},#{type},#{ref},#{operatorId},#{remark})")
    int movement(@Param("productId")Long productId,@Param("currentDelta")int currentDelta,@Param("reservedDelta")int reservedDelta,
                 @Param("beforeQty")int beforeQty,@Param("afterQty")int afterQty,@Param("beforeReserved")int beforeReserved,@Param("afterReserved")int afterReserved,
                 @Param("type")String type,@Param("ref")String ref,@Param("operatorId")Long operatorId,@Param("remark")String remark);
    @Select("<script>SELECT p.id product_id,p.code,p.barcode,p.name,c.name category_name,p.unit,p.low_stock_threshold,i.current_qty,i.reserved_qty,(i.current_qty-i.reserved_qty) available_qty," +
            "CASE WHEN i.current_qty-i.reserved_qty&lt;=p.low_stock_threshold THEN 1 ELSE 0 END low_stock FROM inventory i JOIN product p ON p.id=i.product_id JOIN category c ON c.id=p.category_id " +
            "<where><if test='lowOnly'>AND i.current_qty-i.reserved_qty&lt;=p.low_stock_threshold</if><if test='keyword != null and keyword != &quot;&quot;'>AND (p.name LIKE CONCAT('%',#{keyword},'%') OR p.code=#{keyword} OR p.barcode=#{keyword})</if></where> " +
            "ORDER BY low_stock DESC,p.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> page(@Param("keyword")String keyword,@Param("lowOnly")boolean lowOnly,@Param("offset")int offset,@Param("pageSize")int pageSize);
    @Select("<script>SELECT COUNT(*) FROM inventory i JOIN product p ON p.id=i.product_id <where><if test='lowOnly'>AND i.current_qty-i.reserved_qty&lt;=p.low_stock_threshold</if><if test='keyword != null and keyword != &quot;&quot;'>AND (p.name LIKE CONCAT('%',#{keyword},'%') OR p.code=#{keyword} OR p.barcode=#{keyword})</if></where></script>")
    long count(@Param("keyword")String keyword,@Param("lowOnly")boolean lowOnly);
    @Select("<script>SELECT m.*,p.code product_code,p.name product_name,u.display_name operator_name FROM inventory_movement m JOIN product p ON p.id=m.product_id JOIN sys_user u ON u.id=m.operator_id " +
            "<where><if test='productId != null'>AND m.product_id=#{productId}</if><if test='businessType != null and businessType != &quot;&quot;'>AND m.business_type=#{businessType}</if></where> ORDER BY m.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> movements(@Param("productId")Long productId,@Param("businessType")String businessType,@Param("offset")int offset,@Param("pageSize")int pageSize);
    @Select("<script>SELECT COUNT(*) FROM inventory_movement <where><if test='productId != null'>AND product_id=#{productId}</if><if test='businessType != null and businessType != &quot;&quot;'>AND business_type=#{businessType}</if></where></script>")
    long movementCount(@Param("productId")Long productId,@Param("businessType")String businessType);
}

