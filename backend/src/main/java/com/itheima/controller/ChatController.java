package com.itheima.controller;

import com.itheima.pojo.ChatMessage;
import com.itheima.pojo.ChatMessageRequest;
import com.itheima.pojo.ChatSession;
import com.itheima.pojo.Result;
import com.itheima.service.ChatService;
import com.itheima.service.FolderService;
import com.itheima.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final FolderService folderService;

    @PostMapping("/sessions")
    public Result<ChatSession> createSession(@RequestParam String sessionName) {
        // 从线程本地获取当前用户ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Integer) userInfo.get("id")).longValue();

        ChatSession session = chatService.createSession(userId, sessionName);
        return Result.success(session);
    }

    @GetMapping("/sessions")
    public Result<List<ChatSession>> getSessions() {
        // 从线程本地获取当前用户ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Integer) userInfo.get("id")).longValue();

        List<ChatSession> session = chatService.getUserSessions(userId);
        return Result.success(session);
    }

    @PostMapping("/messages")
    public Result<ChatMessage> sendMessage(
            @RequestParam Long sessionId,
            @RequestBody ChatMessageRequest request) {
        // 从线程本地获取当前用户ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Integer) userInfo.get("id")).longValue();
        
        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        
        try {
            ChatMessage message;
            // 如果指定了文件或文件夹，使用新的方法
            if ((request.getFileIds() != null && !request.getFileIds().isEmpty()) || 
                (request.getFolderIds() != null && !request.getFolderIds().isEmpty())) {
                message = chatService.sendMessage(sessionId, userId, content, 
                    request.getFileIds(), request.getFolderIds());
            } else {
                // 否则使用默认方法（使用user_{userId}路径）
                message = chatService.sendMessage(sessionId, userId, content);
            }
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessageHistory(@RequestParam Long sessionId) {
        List<ChatMessage> messages = chatService.getMessageHistory(sessionId);
        return Result.success(messages);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<String> deleteSession(@PathVariable Long sessionId) {
        boolean success = chatService.deleteSession(sessionId);
        if (success) {
            return Result.success("会话删除成功");
        } else {
            return Result.error("会话删除失败");
        }
    }

    /**
     * 重命名会话
     */
    @PutMapping("/sessions/{sessionId}")
    public Result<ChatSession> renameSession(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> params) {
        String newName = params.get("sessionName");
        if (newName == null || newName.trim().isEmpty()) {
            return Result.error("会话名称不能为空");
        }
        ChatSession session = chatService.renameSession(sessionId, newName);
        return Result.success(session);
    }

    /**
     * 清除会话的聊天历史
     */
    @DeleteMapping("/sessions/{sessionId}/history")
    public Result<String> clearSessionHistory(@PathVariable Long sessionId) {
        // 从线程本地获取当前用户ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Integer) userInfo.get("id")).longValue();
        
        boolean success = chatService.clearSessionHistory(sessionId, userId);
        if (success) {
            return Result.success("会话历史清除成功");
        } else {
            return Result.error("会话历史清除失败，请检查会话是否存在");
        }
    }

    /**
     * 获取指定消息详情
     */
    @GetMapping("/messages/{messageId}")
    public Result<ChatMessage> getMessageById(@PathVariable Long messageId) {
        ChatMessage message = chatService.getMessageById(messageId);
        if (message != null) {
            return Result.success(message);
        } else {
            return Result.error("消息不存在");
        }
    }

    /**
     * 获取用户的文件夹树结构（用于聊天时选择文件）
     */
    @GetMapping("/folders")
    public Result<Map<String, Object>> getUserFoldersForChat() {
        // 从线程本地获取当前用户ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Integer) userInfo.get("id")).longValue();
        
        // 获取用户的文件夹树结构和根目录下的文件
        Map<String, Object> folderContents = folderService.getFolderContents(userId, 0L);
        return Result.success(folderContents);
    }

    /**
     * 测试接口：预览指定文件和文件夹的文档路径
     * 用于调试和验证路径收集功能
     */
    @PostMapping("/preview-paths")
    public Result<List<String>> previewDocumentPaths(@RequestBody ChatMessageRequest request) {
        // 从线程本地获取当前用户ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Integer) userInfo.get("id")).longValue();
        
        try {
            List<String> paths = chatService.previewDocumentPaths(
                userId, request.getFileIds(), request.getFolderIds());
            return Result.success(paths);
        } catch (Exception e) {
            return Result.error("预览路径失败: " + e.getMessage());
        }
    }
}