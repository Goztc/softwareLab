package com.itheima.pojo.rag;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreResponse {
    
    /**
     * 操作状态: created, exists, error
     */
    private String status;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 向量存储路径
     */
    private String path;
    
    /**
     * 元数据信息
     */
    private VectorStoreMetadata metadata;
    
    /**
     * 错误信息（当status为error时）
     */
    private String error;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VectorStoreMetadata {
        @JsonProperty("document_path")
        private String documentPath;
        
        @JsonProperty("document_count")
        private Integer documentCount;
        
        @JsonProperty("chunk_count")
        private Integer chunkCount;
        
        @JsonProperty("created_at")
        private Double createdAt;
        
        @JsonProperty("embedding_model")
        private String embeddingModel;
    }
} 