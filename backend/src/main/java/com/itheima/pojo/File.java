package com.itheima.pojo;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("file")
public class File {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long folderId;
    private String fileName;
    @JsonIgnore
    private String filePath;
    @JsonIgnore
    private String fileType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;

}