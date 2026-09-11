package com.retailable.supermarket.audit;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AuditMapper {
    @Insert("INSERT INTO audit_log(user_id,username,action,resource_type,resource_id,detail,ip_address,success) VALUES(#{userId},#{username},#{action},#{resourceType},#{resourceId},#{detail},#{ipAddress},#{success})")
    int insert(@Param("userId") Long userId, @Param("username") String username, @Param("action") String action,
               @Param("resourceType") String resourceType, @Param("resourceId") String resourceId,
               @Param("detail") String detail, @Param("ipAddress") String ipAddress, @Param("success") boolean success);

    @Select("<script>SELECT * FROM audit_log <where><if test='keyword != null and keyword != &quot;&quot;'>AND (username LIKE CONCAT('%',#{keyword},'%') OR action LIKE CONCAT('%',#{keyword},'%'))</if></where> ORDER BY id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> page(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM audit_log <where><if test='keyword != null and keyword != &quot;&quot;'>AND (username LIKE CONCAT('%',#{keyword},'%') OR action LIKE CONCAT('%',#{keyword},'%'))</if></where></script>")
    long count(String keyword);
}

