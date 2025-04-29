package com.itheima.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_file")
public class UserFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Long folderId;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 