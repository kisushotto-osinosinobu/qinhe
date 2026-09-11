package com.retailable.supermarket.auth;

import com.retailable.supermarket.security.UserAccount;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM sys_user WHERE username=#{username}")
    UserAccount findByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE id=#{id}")
    UserAccount findById(Long id);

    @Insert("INSERT INTO sys_user(username,password_hash,display_name,phone,email,role,status) VALUES(#{username},#{passwordHash},#{displayName},#{phone},#{email},'MEMBER','ACTIVE')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAccount user);

    @Update("UPDATE sys_user SET display_name=#{displayName},phone=#{phone},email=#{email},avatar_url=#{avatarUrl} WHERE id=#{id}")
    int updateProfile(UserAccount user);

    @Update("UPDATE sys_user SET password_hash=#{hash}, token_version=token_version+1 WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("hash") String hash);

    @Update("UPDATE sys_user SET role=#{role}, status=#{status}, token_version=token_version+1 WHERE id=#{id}")
    int updateRoleStatus(@Param("id") Long id, @Param("role") String role, @Param("status") String status);

    @Select("<script>SELECT id,username,display_name,phone,email,avatar_url,role,status,created_at FROM sys_user " +
            "<where><if test='keyword != null and keyword != &quot;&quot;'>AND (username LIKE CONCAT('%',#{keyword},'%') OR display_name LIKE CONCAT('%',#{keyword},'%'))</if></where> " +
            "ORDER BY id DESC LIMIT #{offset},#{pageSize}</script>")
    List<Map<String,Object>> page(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM sys_user <where><if test='keyword != null and keyword != &quot;&quot;'>AND (username LIKE CONCAT('%',#{keyword},'%') OR display_name LIKE CONCAT('%',#{keyword},'%'))</if></where></script>")
    long count(String keyword);

    @Insert("INSERT INTO auth_session(jti,user_id,expires_at) VALUES(#{jti},#{userId},#{expiresAt})")
    int insertSession(@Param("jti") String jti, @Param("userId") Long userId, @Param("expiresAt") LocalDateTime expiresAt);

    @Select("SELECT COUNT(*) FROM auth_session WHERE jti=#{jti} AND user_id=#{userId} AND revoked_at IS NULL AND expires_at>NOW(3)")
    int isSessionActive(@Param("jti") String jti, @Param("userId") Long userId);

    @Update("UPDATE auth_session SET revoked_at=NOW(3) WHERE jti=#{jti} AND revoked_at IS NULL")
    int revokeSession(String jti);

    @Update("UPDATE auth_session SET revoked_at=NOW(3) WHERE user_id=#{userId} AND revoked_at IS NULL")
    int revokeAllSessions(Long userId);
}

