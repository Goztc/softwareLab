package com.itheima.controller;

import com.itheima.pojo.Folder;
import com.itheima.pojo.Result;
import com.itheima.service.FolderService;
import com.itheima.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文件夹管理控制器
 * 实现文件夹的创建、查询、删除等核心功能[6,8](@ref)
 */
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     * @throws IllegalArgumentException 如果用户未登录
     */
    private Long getCurrentUserId() {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        if (userInfo == null || userInfo.get("id") == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return ((Integer) userInfo.get("id")).longValue();
    }

    /**
     * 验证文件夹ID是否有效
     * @param folderId 文件夹ID
     * @throws IllegalArgumentException 如果ID无效
     */
    private void validateFolderId(Long folderId) {
        if (folderId == null || folderId <= 0) {
            throw new IllegalArgumentException("文件夹ID无效");
        }
    }

    /**
     * 验证文件夹名称是否有效
     * @param folderName 文件夹名称
     * @throws IllegalArgumentException 如果名称无效
     */
    private void validateFolderName(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件夹名称不能为空");
        }
    }

    /**
     * 创建新文件夹
     *
     * @param parentId   父文件夹ID（0表示根目录）
     * @param folderName 文件夹名称
     * @return 创建成功的文件夹对象
     */
    @PostMapping("/create")
    public Result createFolder(
            @RequestParam(defaultValue = "0") Long parentId,
            @RequestParam String folderName) {
        try {
            Long userId = getCurrentUserId();
            validateFolderName(folderName);
            if (parentId == null || parentId < 0) {
                throw new IllegalArgumentException("父文件夹ID无效");
            }

            Folder folder = folderService.createFolder(userId, parentId, folderName);
            return Result.success(folder);
        } catch (Exception e) {
            return Result.error("创建文件夹失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户文件夹树形结构
     * @return 嵌套结构的文件夹树
     */
    @GetMapping("/tree")
    public Result<List<Folder>> getFolderTree() {
        try {
            Long userId = getCurrentUserId();
            List<Folder> folderTree = folderService.getFolderTree(userId);
            return Result.success(folderTree);
        } catch (Exception e) {
            return Result.error("获取文件夹树失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件夹及其所有内容
     * @param folderId 要删除的文件夹ID
     * @return 操作结果
     */
    @DeleteMapping("/{folderId}")
    public Result<Void> deleteFolder(@PathVariable Long folderId) {
        try {
            Long userId = getCurrentUserId();
            validateFolderId(folderId);
            folderService.deleteFolderWithContents(userId, folderId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除文件夹失败: " + e.getMessage());
        }
    }

    /**
     * 重命名文件夹
     * @param folderId 文件夹ID
     * @param newName 新文件夹名称
     * @return 更新后的文件夹对象
     */
    @PutMapping("/{folderId}/rename")
    public Result<Folder> renameFolder(
            @PathVariable Long folderId,
            @RequestParam String newName) {
        try {
            Long userId = getCurrentUserId();
            validateFolderId(folderId);
            validateFolderName(newName);
            Folder updatedFolder = folderService.renameFolder(userId, folderId, newName);
            return Result.success(updatedFolder);
        } catch (Exception e) {
            return Result.error("重命名文件夹失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定文件夹内容
     * @param folderId 目标文件夹ID
     * @return 包含子文件夹和文件的复合结构
     */
    @GetMapping("/{folderId}/contents")
    public Result<Map<String, Object>> getFolderContents(
            @PathVariable Long folderId) {
        try {
            Long userId = getCurrentUserId();
            validateFolderId(folderId);
            return Result.success(folderService.getFolderContents(userId, folderId));
        } catch (Exception e) {
            return Result.error("获取文件夹内容失败: " + e.getMessage());
        }
    }
}