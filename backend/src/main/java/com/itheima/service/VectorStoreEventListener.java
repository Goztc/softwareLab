package com.itheima.service;

import com.itheima.service.impl.RagClientServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 向量存储事件监听器
 * 监听文件操作事件，自动触发向量存储的重建
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rag.enabled", havingValue = "true", matchIfMissing = true)
public class VectorStoreEventListener {
    
    private final RagClientService ragClientService;
    
    /**
     * 处理文件上传事件
     * @param userId 用户ID
     */
    public void onFileUploaded(Long userId) {
        log.info("检测到文件上传事件，开始重建向量存储 - 用户: {}", userId);
        ragClientService.buildVectorStoreAsync(userId, true);
    }
    
    /**
     * 处理文件创建事件
     * @param userId 用户ID
     */
    public void onFileCreated(Long userId) {
        log.info("检测到文件创建事件，开始重建向量存储 - 用户: {}", userId);
        ragClientService.buildVectorStoreAsync(userId, true);
    }
    
    /**
     * 处理文件删除事件
     * @param userId 用户ID
     */
    public void onFileDeleted(Long userId) {
        log.info("检测到文件删除事件，开始重建向量存储 - 用户: {}", userId);
        ragClientService.buildVectorStoreAsync(userId, true);
    }
    
    /**
     * 处理文件重命名事件
     * @param userId 用户ID
     */
    public void onFileRenamed(Long userId) {
        log.info("检测到文件重命名事件，开始重建向量存储 - 用户: {}", userId);
        ragClientService.buildVectorStoreAsync(userId, true);
    }
    
    /**
     * 处理文件移动事件
     * @param userId 用户ID
     */
    public void onFileMoved(Long userId) {
        log.info("检测到文件移动事件，开始重建向量存储 - 用户: {}", userId);
        ragClientService.buildVectorStoreAsync(userId, true);
    }
    
    /**
     * 处理首次文件操作事件（如果向量存储不存在则创建）
     * @param userId 用户ID
     */
    public void onFirstFileOperation(Long userId) {
        log.info("检测到首次文件操作，开始构建向量存储 - 用户: {}", userId);
        ragClientService.buildVectorStoreAsync(userId, false);
    }
} 