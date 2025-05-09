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
        if (content == null) {
            return Result.error("消息为空");
        }
        ChatMessage message = chatService.sendMessage(sessionId, userId, content);
        return Result.success(message);
    }

    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessageHistory(@RequestParam Long sessionId) {
        List<ChatMessage> messages = chatService.getMessageHistory(sessionId);
        return Result.success(messages);
    }
}