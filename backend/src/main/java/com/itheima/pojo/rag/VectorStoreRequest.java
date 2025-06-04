package com.itheima.pojo.rag;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreRequest {
    
    /**
     * 文档路径，对应用户ID
     */
    @JsonProperty("document_path")
    private String documentPath;
    
    /**
     * 是否强制重新构建
     */
    @JsonProperty("force_rebuild")
    private boolean forceRebuild;
}