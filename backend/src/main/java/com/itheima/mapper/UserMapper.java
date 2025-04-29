package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO user(username, password, secret_key, create_time, update_time) " +
            "VALUES(#{username}, #{password}, #{secretKey}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET secret_key = #{secretKey}, update_time = #{updateTime} WHERE id = #{id}")
    void updateSecretKey(@Param("id") Long id, @Param("secretKey") String secretKey, @Param("updateTime") LocalDateTime updateTime);
}