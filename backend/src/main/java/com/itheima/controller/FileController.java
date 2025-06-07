// FileController.java
package com.itheima.controller;

import com.itheima.dto.CreateTextFileRequest;
import com.itheima.pojo.File;
import com.itheima.pojo.Result;
import com.itheima.service.FileService;
import com.itheima.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 文件管理控制器
 * 实现文件上传、下载、移动等核心操作[1,4](@ref)
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

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
     * 文件上传接口
     * @param file 上传的文件对象
     * @param folderId 目标文件夹ID
     * @return 上传成功的文件信息
     */
    @PostMapping("/upload")
    public Result<File> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0") Long folderId) throws IOException {
        Long userId = getCurrentUserId();

        File uploadedFile = fileService.uploadFile(userId, folderId, file);
        return Result.success(uploadedFile);
    }

    @PostMapping("/create-text")
    public Result<File> createTextFile(@RequestBody CreateTextFileRequest request) throws IOException {
        Long userId = getCurrentUserId();

        File createdFile = fileService.createTextFile(userId, request.getFolderId(), request.getFileName(), request.getContent());
        return Result.success(createdFile);
    }

    @PutMapping("/{fileId}/rename")
    public Result renameFile(
            @PathVariable Long fileId,
            @RequestParam String newName) {
        try {
            Long userId = getCurrentUserId();
            File renamedFile = fileService.renameFile(userId, fileId, newName);
            return Result.success(renamedFile);
        } catch (Exception e) {
            return Result.error("重命名文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件内容
     * @param fileId 文件ID
     * @return 文件内容（字符串）
     */
    @GetMapping("/{fileId}/content")
    public Result<String> getFileContent(@PathVariable Long fileId) throws IOException {
        Long userId = getCurrentUserId();
        try {
            String content = fileService.getFileContent(userId, fileId);
            return Result.success(content);
        } catch (Exception e) {
            return Result.error("获取文件内容失败: " + e.getMessage());
        }
    }

    /**
     * 移动文件到新文件夹
     * @param fileId 要移动的文件ID
     * @param targetFolderId 目标文件夹ID
     * @return 操作结果
     */
    @PutMapping("/{fileId}/move")
    public Result<Void> moveFile(
            @PathVariable Long fileId,
            @RequestParam Long targetFolderId) {
        Long userId = getCurrentUserId();

        fileService.moveFile(userId, fileId, targetFolderId);
        return Result.success();
    }

    /**
     * 文件下载接口
     * @param fileId 要下载的文件ID
     * @return 文件二进制流（实际实现需配合HttpServletResponse）
     */
    @GetMapping("/{fileId}/download")
    public Result<Void> downloadFile(@PathVariable Long fileId) throws IOException {
        Long userId = getCurrentUserId();

        fileService.downloadFile(userId, fileId);
        return Result.success();
    }

    /**
     * 删除文件
     * @param fileId 要删除的文件ID
     * @return 操作结果
     */
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        Long userId = getCurrentUserId();

        fileService.deleteFile(userId, fileId);
        return Result.success();
    }

    /**
     * 获取文件元数据
     * @param fileId 目标文件ID
     * @return 文件详细信息
     */
    @GetMapping("/{fileId}")
    public Result<File> getFileMetadata(@PathVariable Long fileId) {
        Long userId = getCurrentUserId();

        return Result.success(fileService.getFileMetadata(userId, fileId));
    }
}