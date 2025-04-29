package com.itheima.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_folder")
public class UserFolder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String folderName;
    private Long parentId;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 