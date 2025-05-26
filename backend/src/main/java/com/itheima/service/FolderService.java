package com.itheima.service;

import com.itheima.pojo.Folder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
 * 文件夹管理服务接口
 */
public interface FolderService {

    /**
     * 创建文件夹
     * @param userId 用户ID
     * @param parentId 父文件夹ID（0表示根目录）
     * @param folderName 文件夹名称
     * @return 创建成功的文件夹对象
     */
    @Transactional
    Folder createFolder(Long userId, Long parentId, String folderName);

    /**
     * 获取用户的文件夹树形结构
     * @param userId 用户ID
     * @return 嵌套结构的文件夹树
     */
    List<Folder> getFolderTree(Long userId);

    /**
     * 删除文件夹及其所有内容（递归删除）
     * @param userId 用户ID
     * @param folderId 目标文件夹ID
     */
    @Transactional
    void deleteFolderWithContents(Long userId, Long folderId);

    /**
     * 重命名文件夹
     * @param userId 用户ID
     * @param folderId 目标文件夹ID
     * @param newName 新名称
     * @return 更新后的文件夹对象
     */
    @Transactional
    Folder renameFolder(Long userId, Long folderId, String newName);

    /**
     * 获取文件夹内容（子文件夹+文件）
     * @param userId 用户ID
     * @param folderId 目标文件夹ID
     * @return 包含两个键的Map：
     *         - "folders": List<Folder> 子文件夹列表
     *         - "files": List<File> 文件列表
     */
    Map<String, Object> getFolderContents(Long userId, Long folderId);
}