package com.itheima.service;

import com.itheima.pojo.ChatMessage;
import com.itheima.pojo.ChatSession;

import java.util.List;

/**
 * 聊天服务接口
 * <p>
 * 提供聊天会话和消息管理的核心业务功能
 * </p>
 */
public interface ChatService {

    /**
     * 创建新的聊天会话
     *
     * @param userId      用户ID
     * @param sessionName 会话名称
     * @return 创建的会话对象
     */
    ChatSession createSession(Long userId, String sessionName);

    /**
     * 发送消息并获取AI回复
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param content   消息内容
     * @return AI回复的消息对象
     */
    ChatMessage sendMessage(Long sessionId, Long userId, String content);

    /**
     * 发送消息并获取AI回复（支持指定文件和文件夹）
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param content   消息内容
     * @param fileIds   文件ID列表（可选）
     * @param folderIds 文件夹ID列表（可选）
     * @return AI回复的消息对象
     */
    ChatMessage sendMessage(Long sessionId, Long userId, String content, List<Long> fileIds, List<Long> folderIds);

    /**
     * 获取指定会话的消息历史
     *
     * @param sessionId 会话ID
     * @return 消息历史列表
     */
    List<ChatMessage> getMessageHistory(Long sessionId);

    /**
     * 获取用户的所有会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getUserSessions(Long userId);

    /**
     * 删除指定会话及其所有消息
     *
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    boolean deleteSession(Long sessionId);

    /**
     * 重命名会话
     *
     * @param sessionId   会话ID
     * @param newName     新的会话名称
     * @return 更新后的会话对象
     */
    ChatSession renameSession(Long sessionId, String newName);

    /**
     * 获取指定消息的详细信息
     *
     * @param messageId 消息ID
     * @return 消息对象
     */
    ChatMessage getMessageById(Long messageId);

    /**
     * 清除会话的聊天历史
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 是否清除成功
     */
    boolean clearSessionHistory(Long sessionId, Long userId);

    /**
     * 预览指定文件和文件夹的文档路径
     * 用于调试和验证路径收集功能
     *
     * @param userId    用户ID
     * @param fileIds   文件ID列表
     * @param folderIds 文件夹ID列表
     * @return 文档路径列表
     */
    List<String> previewDocumentPaths(Long userId, List<Long> fileIds, List<Long> folderIds);
}