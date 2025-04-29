package com.itheima.pojo;

import lombok.Data;
import java.util.List;

@Data
public class FolderTree {
    private Long id;
    private String folderName;
    private List<FolderTree> children;
    private List<UserFile> files;
} 