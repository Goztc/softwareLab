package com.itheima.pojo;

import lombok.Data;
import java.util.List;

/**
 * 聊天消息请求DTO
 */
@Data
public class ChatMessageRequest {
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 指定的文件ID列表（可选）
     */
    private List<Long> fileIds;
    
    /**
     * 指定的文件夹ID列表（可选）
     */
    private List<Long> folderIds;
} 