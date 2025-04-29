package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.pojo.UserFile;
import com.itheima.pojo.UserFolder;
import com.itheima.pojo.FolderTree;
import com.itheima.service.FileService;
import com.itheima.utils.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/folders")
    public Result<UserFolder> createFolder(@RequestParam String folderName,
                                         @RequestParam(required = false) Long parentId) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        UserFolder folder = fileService.createFolder(folderName, parentId, userId);
        return Result.success(folder);
    }

    @PostMapping("/upload")
    public Result<UserFile> uploadFile(@RequestParam MultipartFile file,
                                     @RequestParam(required = false) Long folderId) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        UserFile userFile = fileService.uploadFile(file, folderId, userId);
        return Result.success(userFile);
    }

    @GetMapping("/folders")
    public Result<List<UserFolder>> getFolders(@RequestParam(required = false) Long parentId) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        log.info("Getting folders for user: {}, parentId: {}", userId, parentId);
        List<UserFolder> folders = fileService.getFolders(parentId, userId);
        log.info("Found {} folders", folders.size());
        return Result.success(folders);
    }

    @GetMapping("/files")
    public Result<List<UserFile>> getFiles(@RequestParam(required = false) Long folderId) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        log.info("Getting files for user: {}, folderId: {}", userId, folderId);
        List<UserFile> files = fileService.getFiles(folderId, userId);
        log.info("Found {} files", files.size());
        return Result.success(files);
    }

    @GetMapping("/files/{fileId}")
    public Result<UserFile> getFileDetail(@PathVariable Long fileId) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        UserFile file = fileService.getFileDetail(fileId, userId);
        return Result.success(file);
    }

    @GetMapping("/tree")
    public Result<List<FolderTree>> getFolderTree() {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        log.info("Getting folder tree for user: {}", userId);
        List<FolderTree> tree = fileService.getFolderTree(userId);
        log.info("Found {} root folders", tree.size());
        return Result.success(tree);
    }

    @DeleteMapping("/files/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        log.info("Deleting file: {} for user: {}", fileId, userId);
        fileService.deleteFile(fileId, userId);
        return Result.success();
    }

    @DeleteMapping("/folders/{folderId}")
    public Result<Void> deleteFolder(@PathVariable Long folderId) {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = ((Number) userInfo.get("id")).longValue();
        log.info("Deleting folder: {} for user: {}", folderId, userId);
        fileService.deleteFolder(folderId, userId);
        return Result.success();
    }
} 