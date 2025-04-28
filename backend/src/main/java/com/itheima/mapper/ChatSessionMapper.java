package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<ChatSession> selectByUserId(Long userId);
}