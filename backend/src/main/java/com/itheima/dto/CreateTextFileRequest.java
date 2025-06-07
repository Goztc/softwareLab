package com.itheima.dto;

import lombok.Data;

/**
 * 创建文本文件请求DTO
 */
@Data
public class CreateTextFileRequest {
    
    private String fileName;
    
    private String content;
    
    private Long folderId = 0L;
} 