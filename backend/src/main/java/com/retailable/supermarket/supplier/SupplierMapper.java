package com.retailable.supermarket.supplier;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SupplierMapper {
    @Select("<script>SELECT * FROM supplier <where><if test='keyword != null and keyword != &quot;&quot;'>AND (name LIKE CONCAT('%',#{keyword},'%') OR code LIKE CONCAT('%',#{keyword},'%') OR contact_name LIKE CONCAT('%',#{keyword},'%'))</if></where> ORDER BY id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> page(@Param("keyword") String keyword,@Param("offset") int offset,@Param("pageSize") int pageSize);
    @Select("<script>SELECT COUNT(*) FROM supplier <where><if test='keyword != null and keyword != &quot;&quot;'>AND (name LIKE CONCAT('%',#{keyword},'%') OR code LIKE CONCAT('%',#{keyword},'%') OR contact_name LIKE CONCAT('%',#{keyword},'%'))</if></where></script>") long count(String keyword);
    @Select("SELECT * FROM supplier WHERE id=#{id}") Map<String,Object> findById(Long id);
    @Select("SELECT * FROM supplier WHERE status='ENABLED' ORDER BY name") List<Map<String,Object>> enabled();
    @Insert("INSERT INTO supplier(code,name,contact_name,phone,email,address,status,remark) VALUES(#{code},#{name},#{contactName},#{phone},#{email},#{address},#{status},#{remark})")
    @Options(useGeneratedKeys=true,keyProperty="id") int insert(SupplierRow row);
    @Update("UPDATE supplier SET code=#{code},name=#{name},contact_name=#{contactName},phone=#{phone},email=#{email},address=#{address},status=#{status},remark=#{remark} WHERE id=#{id}") int update(SupplierRow row);
    @Select("SELECT COUNT(*) FROM purchase_order WHERE supplier_id=#{id}") int purchaseCount(Long id);
    @Delete("DELETE FROM supplier WHERE id=#{id}") int delete(Long id);
    class SupplierRow {
        public Long id; public String code,name,contactName,phone,email,address,status,remark;
        public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getCode(){return code;} public String getName(){return name;} public String getContactName(){return contactName;} public String getPhone(){return phone;} public String getEmail(){return email;} public String getAddress(){return address;} public String getStatus(){return status;} public String getRemark(){return remark;}
    }
}

