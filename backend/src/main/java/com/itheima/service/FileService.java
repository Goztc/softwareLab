package com.itheima.service;

import com.itheima.pojo.File;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * 文件管理服务接口
 */
public interface FileService {

    /**
     * 文件上传
     * @param userId 用户ID
     * @param folderId 目标文件夹ID（0表示根目录）
     * @param file 上传的文件对象
     * @return 包含文件元数据的实体
     * @throws IOException 文件存储异常
     */
    @Transactional
    File uploadFile(Long userId, Long folderId, MultipartFile file) throws IOException;

    /**
     * 移动文件到新文件夹
     * @param userId 用户ID
     * @param fileId 要移动的文件ID
     * @param targetFolderId 目标文件夹ID
     */
    @Transactional
    void moveFile(Long userId, Long fileId, Long targetFolderId);

    @Transactional
    File renameFile(Long userId, Long fileId, String newName);

    /**
     * 文件下载
     * @param userId 用户ID
     * @param fileId 文件ID
     * @return 可下载的文件资源
     * @throws IOException 文件读取异常
     */
    Resource downloadFile(Long userId, Long fileId) throws IOException;

    /**
     * 删除文件（同时删除物理文件）
     * @param userId 用户ID
     * @param fileId 文件ID
     */
    @Transactional
    void deleteFile(Long userId, Long fileId);

    /**
     * 获取文件元数据
     * @param userId 用户ID
     * @param fileId 文件ID
     * @return 文件实体（不包含文件内容）
     */
    File getFileMetadata(Long userId, Long fileId);

    File createTextFile(Long userId, Long folderId, String fileName, String content) throws IOException;
}