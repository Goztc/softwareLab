package com.itheima.controller;

import com.itheima.pojo.ChatMessage;
import com.itheima.pojo.ChatSession;
import com.itheima.pojo.Result;
import com.itheima.service.ChatService;
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
            @RequestBody Map<String, String> params) {
        // 从线程本地获取当前用户ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Integer) userInfo.get("id")).longValue();
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        try {
            ChatMessage message = chatService.sendMessage(sessionId, userId, content);
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
}