package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.pojo.rag.VectorStoreResponse;
import com.itheima.service.RagClientService;
import com.itheima.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG向量存储管理控制器
 * 提供向量存储的手动管理功能
 */
@Slf4j
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rag.enabled", havingValue = "true", matchIfMissing = true)
public class RagController {
    
    private final RagClientService ragClientService;
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     * @throws IllegalArgumentException 如果用户未登录
     */
    private Long getCurrentUserId() {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        if (userInfo == null || userInfo.get("id") == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return ((Integer) userInfo.get("id")).longValue();
    }
    
    /**
     * 手动构建当前用户的向量存储
     * @param forceRebuild 是否强制重建，默认为false
     * @return 构建结果
     */
    @PostMapping("/build")
    public Result<VectorStoreResponse> buildVectorStore(
            @RequestParam(defaultValue = "false") boolean forceRebuild) {
        try {
            Long userId = getCurrentUserId();
            
            log.info("手动构建向量存储请求 - 用户: {}, 强制重建: {}", userId, forceRebuild);
            
            VectorStoreResponse response = ragClientService.buildVectorStore(userId, forceRebuild);
            
            if ("error".equals(response.getStatus())) {
                return Result.error("构建向量存储失败: " + response.getMessage());
            }
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("手动构建向量存储失败", e);
            return Result.error("构建向量存储失败: " + e.getMessage());
        }
    }
    
    /**
     * 异步构建当前用户的向量存储
     * @param forceRebuild 是否强制重建，默认为false
     * @return 操作结果
     */
    @PostMapping("/build-async")
    public Result<String> buildVectorStoreAsync(
            @RequestParam(defaultValue = "false") boolean forceRebuild) {
        try {
            Long userId = getCurrentUserId();
            
            log.info("异步构建向量存储请求 - 用户: {}, 强制重建: {}", userId, forceRebuild);
            
            ragClientService.buildVectorStoreAsync(userId, forceRebuild);
            
            return Result.success("向量存储构建任务已提交，正在后台处理");
            
        } catch (Exception e) {
            log.error("提交异步构建向量存储任务失败", e);
            return Result.error("提交构建任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除当前用户的向量存储缓存
     * @return 操作结果
     */
    @PostMapping("/clear-cache")
    public Result<String> clearVectorStoreCache() {
        try {
            Long userId = getCurrentUserId();
            
            log.info("清除向量存储缓存请求 - 用户: {}", userId);
            
            ragClientService.clearVectorStoreCache(userId);
            
            return Result.success("向量存储缓存已清除");
            
        } catch (Exception e) {
            log.error("清除向量存储缓存失败", e);
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查RAG服务健康状态
     * @return 健康状态
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> checkRagHealth() {
        try {
            boolean isHealthy = ragClientService.isRagServiceHealthy();
            
            Map<String, Object> status = Map.of(
                    "healthy", isHealthy,
                    "message", isHealthy ? "RAG服务运行正常" : "RAG服务不可用",
                    "timestamp", System.currentTimeMillis()
            );
            
            return Result.success(status);
            
        } catch (Exception e) {
            log.error("检查RAG服务健康状态失败", e);
            return Result.error("检查服务状态失败: " + e.getMessage());
        }
    }
} 