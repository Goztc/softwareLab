package com.itheima.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.itheima.mapper.ChatMessageMapper;
import com.itheima.mapper.ChatSessionMapper;
import com.itheima.pojo.ChatMessage;
import com.itheima.pojo.ChatSession;
import com.itheima.service.ChatService;
import com.itheima.utils.HttpClientUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    @Value("${claude.api.key}")
    private String claudeApiKey;

    @Value("${claude.api.url:https://one.ooo.cool/v1/chat/completions}")
    private String claudeApiUrl;

    @Value("${claude.api.model:claude-3-5-sonnet-20240620}")
    private String claudeModel;

    @Override
    @Transactional
    public ChatSession createSession(Long userId, String sessionName) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionName(sessionName);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(Long sessionId, Long userId, String content) {
        // 保存用户消息
        ChatMessage userMessage = saveMessage(sessionId, userId, content, "user");
        // 调用Claude API获取回复
        String aiResponse = getAIResponse(userId, content);

        // 保存AI回复
        return saveMessage(sessionId, userId, aiResponse, "assistant");
    }

    @Override
    public List<ChatMessage> getMessageHistory(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime);
        return messageMapper.selectList(queryWrapper);
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdateTime);
        return sessionMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean deleteSession(Long sessionId) {
        // 先删除关联的消息
        LambdaQueryWrapper<ChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(ChatMessage::getSessionId, sessionId);
        messageMapper.delete(messageQueryWrapper);

        // 再删除会话
        return sessionMapper.deleteById(sessionId) > 0;
    }

    @Override
    @Transactional
    public ChatSession renameSession(Long sessionId, String newName) {
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getId, sessionId)
                .set(ChatSession::getSessionName, newName)
                .set(ChatSession::getUpdateTime, LocalDateTime.now());

        sessionMapper.update(null, updateWrapper);
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public ChatMessage getMessageById(Long messageId) {
        return messageMapper.selectById(messageId);
    }

    private ChatMessage saveMessage(Long sessionId, Long userId, String content, String role) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setContent(content);
        message.setRole(role);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 更新会话的更新时间
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getId, sessionId)
                .set(ChatSession::getUpdateTime, LocalDateTime.now());
        sessionMapper.update(null, updateWrapper);

        return message;
    }

    private String getAIResponse(Long userId, String userInput) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", claudeModel);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userInput);

            requestBody.put("messages", new Map[]{userMessage});

            JSONObject jsonResponse  = HttpClientUtil.getInstance()
                    .url(claudeApiUrl)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer " + claudeApiKey)
                    .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                    .addHeader("Content-Type", "application/json")
                    .jsonBody(requestBody)
                    .post()
                    .executeForJson();
            log.info("Claude API response: {}", jsonResponse.toJSONString());
            // 解析响应内容
            if (jsonResponse.containsKey("choices")) {
                JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content");
            }

            log.error("Invalid Claude API response format: {}", jsonResponse);
            return "抱歉，获取AI回复时发生错误";
        } catch (Exception e) {
            log.error("调用Claude API失败", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

}