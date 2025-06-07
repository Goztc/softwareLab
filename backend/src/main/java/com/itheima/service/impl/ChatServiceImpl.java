package com.itheima.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.itheima.mapper.ChatMessageMapper;
import com.itheima.mapper.ChatSessionMapper;
import com.itheima.mapper.FileMapper;
import com.itheima.mapper.FolderMapper;
import com.itheima.pojo.ChatMessage;
import com.itheima.pojo.ChatSession;
import com.itheima.pojo.File;
import com.itheima.pojo.Folder;
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
import java.util.stream.Collectors;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;

    @Value("${rag.api.url:http://localhost:5000}")
    private String ragApiUrl;

    @Value("${rag.api.timeout:30}")
    private int ragTimeout;

    @Value("${file.storage.root}")
    private String fileStorageRoot;

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

        // 保存AI回复（保存完整响应到数据库）
        ChatMessage assistantMessage = saveMessage(sessionId, userId, aiResponse, "assistant");
        
        // 为前端返回精简的响应（移除冗余字段）
        assistantMessage.setContent(extractCoreResponseForFrontend(aiResponse));
        
        return assistantMessage;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(Long sessionId, Long userId, String content, List<Long> fileIds, List<Long> folderIds) {
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
        
        // 收集所有文件路径
        List<String> documentPaths = collectDocumentPaths(userId, fileIds, folderIds);
        
        // 保存用户消息
        ChatMessage userMessage = saveMessage(sessionId, userId, content, "user");
        // 调用RAG API获取回复（使用指定的文档路径）
        String aiResponse = getAIResponseWithDocuments(userId, content, sessionId, documentPaths);

        // 保存AI回复（保存完整响应到数据库）
        ChatMessage assistantMessage = saveMessage(sessionId, userId, aiResponse, "assistant");
        
        // 为前端返回精简的响应（移除冗余字段）
        assistantMessage.setContent(extractCoreResponseForFrontend(aiResponse));
        
        return assistantMessage;
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

    /**
     * 从AI回复的JSON格式中提取纯文本答案
     * @param aiResponse AI回复的JSON字符串
     * @return 纯文本答案
     */
    private String extractAnswerFromResponse(String aiResponse) {
        try {
            JSONObject responseJson = JSONObject.parseObject(aiResponse);
            if (responseJson.containsKey("answer")) {
                return responseJson.getString("answer");
            } else if (responseJson.containsKey("response")) {
                return responseJson.getString("response");
            }
        } catch (Exception e) {
            // 如果解析失败，使用原始内容
            log.debug("解析AI回复JSON失败，使用原始内容: {}", e.getMessage());
        }
        return aiResponse;
    }

    /**
     * 根据文件的相对路径查找对应的逻辑路径
     * @param userId 用户ID
     * @param relativePath RAG返回的相对路径
     * @return 逻辑路径（从根文件夹到文件的完整路径）
     */
    private String buildLogicalPath(Long userId, String relativePath) {
        try {
            // 将相对路径转换为绝对路径
            String absolutePath = convertToAbsolutePath(relativePath);
            
            // 根据绝对路径查找文件记录
            LambdaQueryWrapper<File> fileQuery = new LambdaQueryWrapper<>();
            fileQuery.eq(File::getUserId, userId)
                    .eq(File::getFilePath, absolutePath);
            File file = fileMapper.selectOne(fileQuery);
            
            if (file == null) {
                log.warn("未找到文件记录，absolutePath: {}, relativePath: {}, userId: {}", 
                        absolutePath, relativePath, userId);
                return relativePath; // 如果找不到文件记录，返回原始相对路径
            }
            
            // 构建逻辑路径：从根文件夹到文件的完整路径
            StringBuilder logicalPath = new StringBuilder();
            
            // 如果文件在根目录下（folderId为0），直接返回文件名
            if (file.getFolderId() == 0) {
                return file.getFileName();
            }
            
            // 递归构建文件夹路径
            String folderPath = buildFolderPath(file.getFolderId());
            if (!folderPath.isEmpty()) {
                logicalPath.append(folderPath).append("/");
            }
            logicalPath.append(file.getFileName());
            
            return logicalPath.toString();
            
        } catch (Exception e) {
            log.error("构建逻辑路径失败，relativePath: {}, userId: {}", relativePath, userId, e);
            return relativePath; // 出错时返回原始相对路径
        }
    }

    /**
     * 将相对路径转换为绝对路径
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    private String convertToAbsolutePath(String relativePath) {
        try {
            // 确保文件存储根路径以正确的分隔符结尾
            String normalizedRoot = fileStorageRoot.replace("//", "/").replace("\\", "/");
            if (!normalizedRoot.endsWith("/")) {
                normalizedRoot += "/";
            }
            
            // 规范化相对路径
            String normalizedRelative = relativePath.replace("\\", "/");
            if (normalizedRelative.startsWith("/")) {
                normalizedRelative = normalizedRelative.substring(1);
            }
            
            // 组合成绝对路径
            String absolutePath = Paths.get(normalizedRoot + normalizedRelative).toString();
            
            log.debug("路径转换: {} + {} = {}", normalizedRoot, normalizedRelative, absolutePath);
            return absolutePath;
            
        } catch (Exception e) {
            log.error("路径转换失败，relativePath: {}, fileStorageRoot: {}", relativePath, fileStorageRoot, e);
            return relativePath;
        }
    }

    /**
     * 递归构建文件夹路径
     * @param folderId 文件夹ID
     * @return 从根文件夹到当前文件夹的路径
     */
    private String buildFolderPath(Long folderId) {
        if (folderId == null || folderId == 0) {
            return ""; // 根文件夹
        }
        
        try {
            Folder folder = folderMapper.selectById(folderId);
            if (folder == null) {
                log.warn("未找到文件夹记录，folderId: {}", folderId);
                return "";
            }
            
            // 递归获取父文件夹路径
            String parentPath = buildFolderPath(folder.getParentId());
            
            if (parentPath.isEmpty()) {
                return folder.getFolderName();
            } else {
                return parentPath + "/" + folder.getFolderName();
            }
            
        } catch (Exception e) {
            log.error("构建文件夹路径失败，folderId: {}", folderId, e);
            return "";
        }
    }

    /**
     * 处理RAG响应，将相对路径转换为逻辑路径
     * @param userId 用户ID
     * @param ragResponse RAG API的原始响应（包含相对路径）
     * @return 处理后的响应（包含逻辑路径）
     */
    private String processRagResponse(Long userId, String ragResponse) {
        try {
            JSONObject responseJson = JSONObject.parseObject(ragResponse);
            
            // 处理sources字段中的路径，将相对路径转换为逻辑路径
            if (responseJson.containsKey("sources")) {
                com.alibaba.fastjson.JSONArray sources = responseJson.getJSONArray("sources");
                for (int i = 0; i < sources.size(); i++) {
                    JSONObject source = sources.getJSONObject(i);
                    if (source.containsKey("source")) {
                        String relativePath = source.getString("source");
                        String logicalPath = buildLogicalPath(userId, relativePath);
                        source.put("source", logicalPath);
                        log.debug("路径转换完成: {} -> {}", relativePath, logicalPath);
                    }
                }
            }
            
            return responseJson.toJSONString();
            
        } catch (Exception e) {
            log.error("处理RAG响应失败，userId: {}", userId, e);
            return ragResponse; // 出错时返回原始响应
        }
    }

    /**
     * 提取核心响应信息用于前端展示（移除冗余字段）
     * @param ragResponse 完整的RAG响应
     * @return 精简的响应，只包含answer和sources
     */
    private String extractCoreResponseForFrontend(String ragResponse) {
        try {
            JSONObject responseJson = JSONObject.parseObject(ragResponse);
            JSONObject coreResponse = new JSONObject();
            
            // 只保留核心字段
            if (responseJson.containsKey("answer")) {
                coreResponse.put("answer", responseJson.getString("answer"));
            } else if (responseJson.containsKey("response")) {
                coreResponse.put("answer", responseJson.getString("response"));
            }
            
            if (responseJson.containsKey("sources")) {
                coreResponse.put("sources", responseJson.getJSONArray("sources"));
            } else {
                coreResponse.put("sources", new com.alibaba.fastjson.JSONArray());
            }
            
            return coreResponse.toJSONString();
            
        } catch (Exception e) {
            log.error("提取核心响应失败", e);
            return ragResponse; // 出错时返回原始响应
        }
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
                            historyItem.put("answer", extractAnswerFromResponse(assistantMsg.getContent()));
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
            
            // 解析响应内容 - 返回完整的JSON响应而不是只返回文本
            if (jsonResponse.containsKey("answer") || jsonResponse.containsKey("response")) {
                // 处理响应中的路径信息，将物理路径转换为逻辑路径
                String processedResponse = processRagResponse(userId, jsonResponse.toJSONString());
                return processedResponse;
            }

            log.error("Invalid RAG API response format: {}", jsonResponse);
            return "{\"answer\":\"抱歉，获取AI回复时发生错误\",\"sources\":[]}";
        } catch (Exception e) {
            log.error("调用RAG API失败", e);
            return "{\"answer\":\"抱歉，AI服务暂时不可用，请稍后再试。\",\"sources\":[]}";
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

    /**
     * 收集指定文件和文件夹的所有文档路径
     * @param userId 用户ID
     * @param fileIds 文件ID列表
     * @param folderIds 文件夹ID列表
     * @return 文档路径列表
     */
    private List<String> collectDocumentPaths(Long userId, List<Long> fileIds, List<Long> folderIds) {
        List<String> documentPaths = new ArrayList<>();
        
        // 如果文件和文件夹ID都为空，使用默认的user_{userId}路径
        if ((fileIds == null || fileIds.isEmpty()) && (folderIds == null || folderIds.isEmpty())) {
            documentPaths.add("user_" + userId);
            return documentPaths;
        }
        
        // 处理指定的文件
        if (fileIds != null && !fileIds.isEmpty()) {
            List<File> files = fileMapper.selectBatchIds(fileIds);
            for (File file : files) {
                // 验证文件归属
                if (file.getUserId().equals(userId)) {
                    documentPaths.add(file.getFilePath());
                }
            }
        }
        
        // 处理指定的文件夹（递归获取所有文件）
        if (folderIds != null && !folderIds.isEmpty()) {
            for (Long folderId : folderIds) {
                List<String> folderPaths = collectFolderPaths(userId, folderId);
                documentPaths.addAll(folderPaths);
            }
        }
        
        return documentPaths;
    }

    /**
     * 递归收集文件夹中的所有文件路径
     * @param userId 用户ID
     * @param folderId 文件夹ID
     * @return 文件路径列表
     */
    private List<String> collectFolderPaths(Long userId, Long folderId) {
        List<String> paths = new ArrayList<>();
        
        // 验证文件夹归属
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            log.warn("文件夹不存在或无权限访问，folderId: {}, userId: {}", folderId, userId);
            return paths;
        }
        
        // 获取当前文件夹下的所有文件
        LambdaQueryWrapper<File> fileQuery = new LambdaQueryWrapper<>();
        fileQuery.eq(File::getFolderId, folderId)
                .eq(File::getUserId, userId);
        List<File> files = fileMapper.selectList(fileQuery);
        
        for (File file : files) {
            paths.add(file.getFilePath());
        }
        
        // 递归处理子文件夹
        LambdaQueryWrapper<Folder> folderQuery = new LambdaQueryWrapper<>();
        folderQuery.eq(Folder::getParentId, folderId)
                .eq(Folder::getUserId, userId);
        List<Folder> subFolders = folderMapper.selectList(folderQuery);
        
        for (Folder subFolder : subFolders) {
            paths.addAll(collectFolderPaths(userId, subFolder.getId()));
        }
        
        return paths;
    }

    /**
     * 调用RAG API获取回复（使用指定的文档路径列表）
     * @param userId 用户ID
     * @param userInput 用户输入
     * @param sessionId 会话ID
     * @param documentPaths 文档路径列表
     * @return AI回复
     */
    private String getAIResponseWithDocuments(Long userId, String userInput, Long sessionId, List<String> documentPaths) {
        try {
            // 获取当前会话的历史消息
            List<ChatMessage> historyMessages = getMessageHistory(sessionId);
            
            // 构建请求体调用Flask RAG后端
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", userInput);
            
            // 设置文档路径（如果为空或只有一个路径则使用字符串，否则使用数组）
            if (documentPaths.isEmpty()) {
                requestBody.put("document_path", "user_" + userId);
            } else if (documentPaths.size() == 1) {
                requestBody.put("document_path", documentPaths.get(0));
            } else {
                requestBody.put("document_path", documentPaths);
            }
            
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
                            historyItem.put("answer", extractAnswerFromResponse(assistantMsg.getContent()));
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
            log.info("使用的文档路径: {}", documentPaths);
            
            // 解析响应内容 - 返回完整的JSON响应而不是只返回文本
            if (jsonResponse.containsKey("answer") || jsonResponse.containsKey("response")) {
                // 处理响应中的路径信息，将物理路径转换为逻辑路径
                String processedResponse = processRagResponse(userId, jsonResponse.toJSONString());
                return processedResponse;
            }

            log.error("Invalid RAG API response format: {}", jsonResponse);
            return "{\"answer\":\"抱歉，获取AI回复时发生错误\",\"sources\":[]}";
        } catch (Exception e) {
            log.error("调用RAG API失败", e);
            return "{\"answer\":\"抱歉，AI服务暂时不可用，请稍后再试。\",\"sources\":[]}";
        }
    }

    @Override
    public List<String> previewDocumentPaths(Long userId, List<Long> fileIds, List<Long> folderIds) {
        return collectDocumentPaths(userId, fileIds, folderIds);
    }
}