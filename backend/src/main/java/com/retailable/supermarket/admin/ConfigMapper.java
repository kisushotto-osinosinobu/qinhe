package com.retailable.supermarket.admin;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConfigMapper {
    @Select("SELECT * FROM system_config ORDER BY config_key") List<Map<String,Object>> all();
    @Insert("INSERT INTO system_config(config_key,config_value,description,updated_by) VALUES(#{key},#{value},#{description},#{userId}) ON DUPLICATE KEY UPDATE config_value=VALUES(config_value),description=VALUES(description),updated_by=VALUES(updated_by)")
    int upsert(@Param("key")String key,@Param("value")String value,@Param("description")String description,@Param("userId")Long userId);
}

