package com.retailable.supermarket.catalog;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {
    String BASE = "SELECT p.*,c.name category_name,COALESCE(i.current_qty,0) current_qty,COALESCE(i.reserved_qty,0) reserved_qty," +
            "COALESCE(i.current_qty-i.reserved_qty,0) available_qty FROM product p JOIN category c ON c.id=p.category_id LEFT JOIN inventory i ON i.product_id=p.id ";

    @Select("<script>" + BASE + "<where>" +
            "<if test='publicOnly'>AND p.status='ON_SALE' AND c.status='ENABLED'</if>" +
            "<if test='categoryId != null'>AND p.category_id=#{categoryId}</if>" +
            "<if test='keyword != null and keyword != &quot;&quot;'>AND (p.name LIKE CONCAT('%',#{keyword},'%') OR p.code=#{keyword} OR p.barcode=#{keyword})</if>" +
            "</where> ORDER BY p.id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> page(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                                  @Param("publicOnly") boolean publicOnly, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM product p JOIN category c ON c.id=p.category_id <where>" +
            "<if test='publicOnly'>AND p.status='ON_SALE' AND c.status='ENABLED'</if>" +
            "<if test='categoryId != null'>AND p.category_id=#{categoryId}</if>" +
            "<if test='keyword != null and keyword != &quot;&quot;'>AND (p.name LIKE CONCAT('%',#{keyword},'%') OR p.code=#{keyword} OR p.barcode=#{keyword})</if>" +
            "</where></script>")
    long count(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("publicOnly") boolean publicOnly);

    @Select(BASE + "WHERE p.id=#{id}") Map<String,Object> findById(Long id);
    @Select(BASE + "WHERE p.barcode=#{barcode}") Map<String,Object> findByBarcode(String barcode);

    @Insert("INSERT INTO product(code,barcode,name,category_id,specification,unit,purchase_price,sale_price,image_url,status,low_stock_threshold) " +
            "VALUES(#{code},#{barcode},#{name},#{categoryId},#{specification},#{unit},#{purchasePrice},#{salePrice},#{imageUrl},#{status},#{lowStockThreshold})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductRow row);

    @Insert("INSERT INTO inventory(product_id,current_qty,reserved_qty) VALUES(#{productId},0,0)")
    int initInventory(Long productId);

    @Update("UPDATE product SET code=#{code},barcode=#{barcode},name=#{name},category_id=#{categoryId},specification=#{specification},unit=#{unit}," +
            "purchase_price=#{purchasePrice},sale_price=#{salePrice},image_url=#{imageUrl},status=#{status},low_stock_threshold=#{lowStockThreshold},version=version+1 WHERE id=#{id}")
    int update(ProductRow row);

    class ProductRow {
        public Long id; public String code; public String barcode; public String name; public Long categoryId;
        public String specification; public String unit; public BigDecimal purchasePrice; public BigDecimal salePrice;
        public String imageUrl; public String status; public int lowStockThreshold;
        public Long getId(){return id;} public void setId(Long id){this.id=id;}
        public String getCode(){return code;} public String getBarcode(){return barcode;} public String getName(){return name;}
        public Long getCategoryId(){return categoryId;} public String getSpecification(){return specification;} public String getUnit(){return unit;}
        public BigDecimal getPurchasePrice(){return purchasePrice;} public BigDecimal getSalePrice(){return salePrice;}
        public String getImageUrl(){return imageUrl;} public String getStatus(){return status;} public int getLowStockThreshold(){return lowStockThreshold;}
    }
}

