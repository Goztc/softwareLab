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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    @Value("${rag.api.url:http://localhost:5000}")
    private String ragApiUrl;

    @Value("${rag.api.timeout:30}")
    private int ragTimeout;

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
        // 验证会话是否存在
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            log.error("会话不存在，sessionId: {}, userId: {}", sessionId, userId);
            throw new RuntimeException("会话不存在，sessionId: " + sessionId);
        }
        
        // 验证会话是否属于当前用户
        if (!session.getUserId().equals(userId)) {
            log.error("会话不属于当前用户，sessionId: {}, userId: {}, sessionUserId: {}", 
                    sessionId, userId, session.getUserId());
            throw new RuntimeException("无权限访问该会话");
        }
        
        // 保存用户消息
        ChatMessage userMessage = saveMessage(sessionId, userId, content, "user");
        // 调用RAG API获取回复
        String aiResponse = getAIResponse(userId, content, sessionId);

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
        // 验证会话是否存在（双重保险）
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            log.error("保存消息时发现会话不存在，sessionId: {}, userId: {}", sessionId, userId);
            throw new RuntimeException("会话不存在，无法保存消息");
        }
        
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

    private String getAIResponse(Long userId, String userInput, Long sessionId) {
        try {
            // 获取当前会话的历史消息
            List<ChatMessage> historyMessages = getMessageHistory(sessionId);
            
            // 构建请求体调用Flask RAG后端
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", userInput);
            // 根据用户ID动态设置document_path
            requestBody.put("document_path", "user_" + userId);
            requestBody.put("conversation_id", "session_" + sessionId);
            
            // 构建历史对话数据
            if (!historyMessages.isEmpty()) {
                List<Map<String, Object>> history = new ArrayList<>();
                
                // 将消息配对成问答对，限制最近10轮对话
                for (int i = 0; i < historyMessages.size() - 1; i += 2) {
                    ChatMessage userMsg = historyMessages.get(i);
                    if (i + 1 < historyMessages.size()) {
                        ChatMessage assistantMsg = historyMessages.get(i + 1);
                        
                        // 确保是正确的用户-助手配对
                        if ("user".equals(userMsg.getRole()) && "assistant".equals(assistantMsg.getRole())) {
                            Map<String, Object> historyItem = new HashMap<>();
                            historyItem.put("question", userMsg.getContent());
                            historyItem.put("answer", assistantMsg.getContent());
                            historyItem.put("timestamp", assistantMsg.getCreateTime().toString());
                            history.add(historyItem);
                            
                            // 限制历史记录数量，避免请求过大
                            if (history.size() >= 10) {
                                break;
                            }
                        }
                    }
                }
                
                if (!history.isEmpty()) {
                    requestBody.put("history", history);
                    log.info("传递历史对话记录数量: {}", history.size());
                }
            }

            JSONObject jsonResponse = HttpClientUtil.getInstance()
                    .url(ragApiUrl + "/chat")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .jsonBody(requestBody)
                    .post()
                    .executeForJson();
            
            log.info("RAG API response: {}", jsonResponse.toJSONString());
            
            // 解析响应内容
            if (jsonResponse.containsKey("answer")) {
                return jsonResponse.getString("answer");
            } else if (jsonResponse.containsKey("response")) {
                return jsonResponse.getString("response");
            }

            log.error("Invalid RAG API response format: {}", jsonResponse);
            return "抱歉，获取AI回复时发生错误";
        } catch (Exception e) {
            log.error("调用RAG API失败", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    /**
     * 获取或创建会话
     * 如果会话不存在，则自动创建一个默认会话
     */
    private ChatSession getOrCreateSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            log.warn("会话不存在，自动创建新会话，sessionId: {}, userId: {}", sessionId, userId);
            session = new ChatSession();
            session.setId(sessionId);
            session.setUserId(userId);
            session.setSessionName("默认会话");
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.insert(session);
        }
        return session;
    }

    /**
     * 清除会话的聊天历史
     * 同时通知RAG后端清除对话历史
     */
    @Override
    @Transactional
    public boolean clearSessionHistory(Long sessionId, Long userId) {
        // 验证会话存在且属于当前用户
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return false;
        }

        // 删除数据库中的消息记录
        LambdaQueryWrapper<ChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(ChatMessage::getSessionId, sessionId);
        messageMapper.delete(messageQueryWrapper);

        // 通知RAG后端清除对话历史
        try {
            HttpClientUtil.getInstance()
                    .url(ragApiUrl + "/conversations/session_" + sessionId + "/clear")
                    .addHeader("Content-Type", "application/json")
                    .post()
                    .execute();
            log.info("已通知RAG后端清除会话历史，sessionId: {}", sessionId);
        } catch (Exception e) {
            log.warn("通知RAG后端清除对话历史失败，sessionId: {}", sessionId, e);
            // 即使RAG后端清除失败，数据库的清除仍然成功
        }

        // 更新会话时间
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getId, sessionId)
                .set(ChatSession::getUpdateTime, LocalDateTime.now());
        sessionMapper.update(null, updateWrapper);

        return true;
    }

}