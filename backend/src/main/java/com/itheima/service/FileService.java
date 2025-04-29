package com.itheima.service;

import com.itheima.pojo.UserFile;
import com.itheima.pojo.UserFolder;
import com.itheima.pojo.FolderTree;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    // 创建文件夹
    UserFolder createFolder(String folderName, Long parentId, Long userId);
    
    // 上传文件
    UserFile uploadFile(MultipartFile file, Long folderId, Long userId);
    
    // 获取文件夹内容
    List<UserFolder> getFolders(Long parentId, Long userId);
    List<UserFile> getFiles(Long folderId, Long userId);
    
    // 获取文件详情
    UserFile getFileDetail(Long fileId, Long userId);

    // 获取文件夹树
    List<FolderTree> getFolderTree(Long userId);

    // 删除文件
    void deleteFile(Long fileId, Long userId);

    // 删除文件夹
    void deleteFolder(Long folderId, Long userId);
} 