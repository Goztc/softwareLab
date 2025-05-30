package com.itheima.service.impl;

import com.itheima.config.RagClientConfig;
import com.itheima.pojo.rag.VectorStoreRequest;
import com.itheima.pojo.rag.VectorStoreResponse;
import com.itheima.service.RagClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagClientServiceImpl implements RagClientService {
    
    private final RestTemplate ragRestTemplate;
    private final RagClientConfig ragConfig;
    
    @Override
    public VectorStoreResponse buildVectorStore(Long userId, boolean forceRebuild) {
        if (!ragConfig.isEnabled()) {
            log.warn("RAG服务已禁用，跳过向量存储构建");
            return createDisabledResponse();
        }
        
        return executeWithRetry(() -> {
            String url = ragConfig.getBaseUrl() + "/vectorstores";
            
            VectorStoreRequest request = VectorStoreRequest.builder()
                    .documentPath("user_" + userId)
                    .forceRebuild(forceRebuild)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<VectorStoreRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("调用RAG API构建向量存储 - 用户: {}, 强制重建: {}, 请求路径: user_{}", userId, forceRebuild, userId);
            
            ResponseEntity<VectorStoreResponse> response = ragRestTemplate.exchange(
                    url, HttpMethod.POST, entity, VectorStoreResponse.class);
            
            VectorStoreResponse responseBody = response.getBody();
            if (responseBody != null) {
                log.info("向量存储构建完成 - 用户: {}, 状态: {}, 消息: {}", 
                        userId, responseBody.getStatus(), responseBody.getMessage());
            }
            
            return responseBody;
        }, "构建向量存储", userId);
    }
    
    @Override
    @Async
    public void buildVectorStoreAsync(Long userId, boolean forceRebuild) {
        log.info("异步构建向量存储开始 - 用户: {}", userId);
        
        try {
            VectorStoreResponse response = buildVectorStore(userId, forceRebuild);
            
            if ("error".equals(response.getStatus())) {
                log.error("异步构建向量存储失败 - 用户: {}, 错误: {}", userId, response.getMessage());
            } else {
                log.info("异步构建向量存储成功 - 用户: {}", userId);
            }
        } catch (Exception e) {
            log.error("异步构建向量存储过程中发生异常 - 用户: {}", userId, e);
        }
    }
    
    @Override
    public void clearVectorStoreCache(Long userId) {
        if (!ragConfig.isEnabled()) {
            log.warn("RAG服务已禁用，跳过缓存清除");
            return;
        }
        
        executeWithRetry(() -> {
            String url = ragConfig.getBaseUrl() + "/vectorstores/clear-cache";
            
            Map<String, String> request = new HashMap<>();
            request.put("document_path", "user_" + userId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            log.info("清除向量存储缓存 - 用户: {}", userId);
            
            ragRestTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            log.info("向量存储缓存清除成功 - 用户: {}", userId);
            return null;
        }, "清除向量存储缓存", userId);
    }
    
    @Override
    public boolean isRagServiceHealthy() {
        if (!ragConfig.isEnabled()) {
            return false;
        }
        
        try {
            String url = ragConfig.getBaseUrl() + "/health";
            ResponseEntity<Map> response = ragRestTemplate.getForEntity(url, Map.class);
            
            Map<String, Object> responseBody = response.getBody();
            boolean isHealthy = responseBody != null && "healthy".equals(responseBody.get("status"));
            
            log.debug("RAG服务健康检查结果: {}", isHealthy);
            return isHealthy;
            
        } catch (Exception e) {
            log.warn("RAG服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 带重试机制的执行方法
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation, String operationName, Long userId) {
        int maxAttempts = ragConfig.getRetry().getMaxAttempts();
        long delay = ragConfig.getRetry().getDelay();
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (RestClientException e) {
                lastException = e;
                log.warn("{}失败 - 用户: {}, 尝试次数: {}/{}, 错误: {}", 
                        operationName, userId, attempt, maxAttempts, e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                // 对于非网络异常，不进行重试
                log.error("{}时发生非网络异常 - 用户: {}", operationName, userId, e);
                return (T) createErrorResponse(operationName + "失败: " + e.getMessage());
            }
        }
        
        log.error("{}失败，已达最大重试次数 - 用户: {}", operationName, userId, lastException);
        return (T) createErrorResponse(operationName + "失败: " + 
                (lastException != null ? lastException.getMessage() : "未知错误"));
    }
    
    /**
     * 可重试的操作接口
     */
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
    
    private VectorStoreResponse createDisabledResponse() {
        VectorStoreResponse response = new VectorStoreResponse();
        response.setStatus("disabled");
        response.setMessage("RAG服务已禁用");
        return response;
    }
    
    private VectorStoreResponse createErrorResponse(String errorMessage) {
        VectorStoreResponse response = new VectorStoreResponse();
        response.setStatus("error");
        response.setMessage(errorMessage);
        response.setError(errorMessage);
        return response;
    }
}