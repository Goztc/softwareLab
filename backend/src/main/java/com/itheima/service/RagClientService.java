package com.itheima.service;

import com.itheima.pojo.rag.VectorStoreResponse;

/**
 * RAG客户端服务接口
 * 提供向量存储管理功能
 */
public interface RagClientService {
    
    /**
     * 构建或重建用户的向量存储
     * @param userId 用户ID
     * @param forceRebuild 是否强制重建
     * @return 向量存储响应
     */
    VectorStoreResponse buildVectorStore(Long userId, boolean forceRebuild);
    
    /**
     * 异步构建或重建用户的向量存储
     * @param userId 用户ID
     * @param forceRebuild 是否强制重建
     */
    void buildVectorStoreAsync(Long userId, boolean forceRebuild);
    
    /**
     * 清除用户的向量存储缓存
     * @param userId 用户ID
     */
    void clearVectorStoreCache(Long userId);
    
    /**
     * 检查RAG服务是否健康
     * @return 是否健康
     */
    boolean isRagServiceHealthy();
} 