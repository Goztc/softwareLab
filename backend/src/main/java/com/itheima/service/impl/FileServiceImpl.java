package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mapper.FileMapper;
import com.itheima.pojo.File;
import com.itheima.service.FileService;
import com.itheima.service.VectorStoreEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    @Value("${file.storage.root}")
    private String storageRoot;

    private final FolderServiceImpl folderService;
    
    @Autowired(required = false)
    private VectorStoreEventListener vectorStoreEventListener;

    // ========== 公共业务方法 ==========

    @Override
    @Transactional
    public File uploadFile(Long userId, Long folderId, MultipartFile file) throws IOException {
        validateFolderAccess(userId, folderId);

        Path storagePath = createStoragePath(userId);
        String fileName = file.getOriginalFilename();
        String uniqueName = generateUniqueFilename(fileName);
        File fileEntity = buildFileEntity(userId, folderId, fileName, storagePath, uniqueName);

        saveFileToDisk(file, storagePath.resolve(uniqueName));
        baseMapper.insert(fileEntity);

        // 触发向量存储重建事件
        if (vectorStoreEventListener != null) {
            vectorStoreEventListener.onFileUploaded(userId);
        }

        return fileEntity;
    }

    @Override
    @Transactional
    public void moveFile(Long userId, Long fileId, Long targetFolderId) {
        File file = validateFileOwnership(userId, fileId);
        validateFolderAccess(userId, targetFolderId);

        file.setFolderId(targetFolderId);
        baseMapper.updateById(file);
        
        // 触发向量存储重建事件
        if (vectorStoreEventListener != null) {
            vectorStoreEventListener.onFileMoved(userId);
        }
    }

    @Override
    @Transactional
    public File renameFile(Long userId, Long fileId, String newName) {
        validateFileName(newName);
        File file = validateFileOwnership(userId, fileId);

        // 保留原文件扩展名
        String extension = getFileExtension(file.getFileName());
        String newFileName = newName + (extension != null ? "." + extension : "");

        file.setFileName(newFileName);
        baseMapper.updateById(file);

        // 触发向量存储重建事件
        if (vectorStoreEventListener != null) {
            vectorStoreEventListener.onFileRenamed(userId);
        }

        return file;
    }

    @Override
    public Resource downloadFile(Long userId, Long fileId) throws IOException {
        File file = validateFileOwnership(userId, fileId);
        Path filePath = getPhysicalFilePath(file);

        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在于存储系统: " + filePath);
        }
        return new FileSystemResource(filePath);
    }

    @Override
    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        File file = validateFileOwnership(userId, fileId);

        baseMapper.deleteById(fileId);
        deleteFileFromDisk(file);
        
        // 触发向量存储重建事件
        if (vectorStoreEventListener != null) {
            vectorStoreEventListener.onFileDeleted(userId);
        }
    }

    @Override
    public File getFileMetadata(Long userId, Long fileId) {
        return validateFileOwnership(userId, fileId);
    }

    @Override
    @Transactional
    public File createTextFile(Long userId, Long folderId, String fileName, String content) throws IOException {
        validateFolderAccess(userId, folderId);

        // 确保文件名有.txt扩展名
        String finalFileName = fileName.endsWith(".txt") ? fileName : fileName + ".txt";

        Path storagePath = createStoragePath(userId);
        String uniqueName = generateUniqueFilename(finalFileName);
        File fileEntity = buildFileEntity(userId, folderId, finalFileName, storagePath, uniqueName);

        saveContentToFile(content, storagePath.resolve(uniqueName));
        baseMapper.insert(fileEntity);

        // 触发向量存储重建事件
        if (vectorStoreEventListener != null) {
            vectorStoreEventListener.onFileCreated(userId);
        }

        return fileEntity;
    }

    @Override
    public String getFileContent(Long userId, Long fileId) throws IOException {
        File file = validateFileOwnership(userId, fileId);

        // 检查文件是否为文本类型
        String fileName = file.getFileName().toLowerCase();
        if (!fileName.endsWith(".txt")) {
            throw new IllegalArgumentException("仅支持读取 .txt 文件内容");
        }

        Path filePath = getPhysicalFilePath(file);
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在于存储系统: " + filePath);
        }

        // 读取文件内容
        String content = Files.readString(filePath);
        log.info("文件内容读取成功: {}", filePath);
        return content;
    }

    // ========== 私有辅助方法 ==========

    private void validateFolderAccess(Long userId, Long folderId) {
        if (folderId != 0) {
            folderService.validateFolderOwnership(userId, folderId);
        }
    }

    private Path createStoragePath(Long userId) throws IOException {
        Path path = Paths.get(storageRoot,
                "user_" + userId,
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        Files.createDirectories(path);
        return path;
    }

    private File buildFileEntity(Long userId, Long folderId, String fileName, Path storagePath, String uniqueName) {
        return File.builder()
                .userId(userId)
                .folderId(folderId)
                .fileName(fileName)
                .uploadTime(LocalDateTime.now())
                .filePath(storagePath.resolve(uniqueName).toString())
                .build();
    }

    private void saveFileToDisk(MultipartFile file, Path targetPath) throws IOException {
        file.transferTo(targetPath);
        log.info("文件保存成功: {}", targetPath);
    }

    private void saveContentToFile(String content, Path filePath) throws IOException {
        Files.writeString(filePath, content);
        log.info("文本文件创建成功: {}", filePath);
    }

    private void deleteFileFromDisk(File file) {
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
            log.info("物理文件删除成功: {}", file.getFilePath());
        } catch (IOException e) {
            log.error("物理文件删除失败: {}", file.getFilePath(), e);
            throw new RuntimeException("物理文件删除失败", e);
        }
    }

    private Path getPhysicalFilePath(File file) {
        return Paths.get(file.getFilePath());
    }

    private String generateUniqueFilename(String originalName) {
        return UUID.randomUUID() + "_" + originalName;
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("文件名不能包含路径分隔符");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : null;
    }

    private File validateFileOwnership(Long userId, Long fileId) {
        LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
        query.eq(File::getId, fileId)
                .eq(File::getUserId, userId);

        File file = baseMapper.selectOne(query);
        if (file == null) {
            throw new RuntimeException("文件不存在或权限不足");
        }
        return file;
    }
}