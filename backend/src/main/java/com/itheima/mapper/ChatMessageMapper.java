package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.pojo.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time")
    List<ChatMessage> selectBySessionId(Long sessionId);

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time")
    IPage<ChatMessage> selectPageBySessionId(Page<ChatMessage> page, Long sessionId);
}