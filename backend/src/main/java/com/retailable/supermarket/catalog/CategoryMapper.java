package com.retailable.supermarket.catalog;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper {
    @Select("SELECT c.*,COUNT(p.id) product_count FROM category c LEFT JOIN product p ON p.category_id=c.id " +
            "WHERE (#{keyword} IS NULL OR #{keyword}='' OR c.name LIKE CONCAT('%',#{keyword},'%') OR c.code LIKE CONCAT('%',#{keyword},'%')) " +
            "GROUP BY c.id ORDER BY c.sort_order,c.id LIMIT #{offset},#{pageSize}")
    List<Map<String,Object>> page(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM category WHERE (#{keyword} IS NULL OR #{keyword}='' OR name LIKE CONCAT('%',#{keyword},'%') OR code LIKE CONCAT('%',#{keyword},'%'))")
    long count(String keyword);

    @Select("SELECT * FROM category WHERE id=#{id}") Map<String,Object> findById(Long id);

    @Select("SELECT * FROM category WHERE status='ENABLED' ORDER BY sort_order,id") List<Map<String,Object>> enabled();

    @Insert("INSERT INTO category(name,code,sort_order,status) VALUES(#{name},#{code},#{sortOrder},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CategoryRow row);

    @Update("UPDATE category SET name=#{name},code=#{code},sort_order=#{sortOrder},status=#{status} WHERE id=#{id}")
    int update(CategoryRow row);

    @Select("SELECT COUNT(*) FROM product WHERE category_id=#{id}") int productCount(Long id);
    @Delete("DELETE FROM category WHERE id=#{id}") int delete(Long id);

    class CategoryRow {
        public Long id; public String name; public String code; public int sortOrder; public String status;
        public Long getId(){return id;} public void setId(Long id){this.id=id;}
        public String getName(){return name;} public String getCode(){return code;} public int getSortOrder(){return sortOrder;} public String getStatus(){return status;}
    }
}

