package com.itheima.service.impl;

import com.itheima.mapper.UserFileMapper;
import com.itheima.mapper.UserFolderMapper;
import com.itheima.mapper.UserMapper;
import com.itheima.pojo.User;
import com.itheima.pojo.UserFile;
import com.itheima.pojo.UserFolder;
import com.itheima.pojo.FolderTree;
import com.itheima.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Value("${file.upload.path}")
    private String uploadPath;

    private final UserFolderMapper folderMapper;
    private final UserFileMapper fileMapper;
    private final UserMapper userMapper;

    public FileServiceImpl(UserFolderMapper folderMapper, UserFileMapper fileMapper, UserMapper userMapper) {
        this.folderMapper = folderMapper;
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
    }

    private String getUserUploadPath(Long userId) {
        log.info("Getting upload path for user: {}", userId);
        User user = userMapper.findById(userId);
        log.info("Found user: {}", user);
        if (user == null || user.getSecretKey() == null) {
            throw new RuntimeException("用户不存在或未分配密钥");
        }
        String path = uploadPath + File.separator + userId + "_" + user.getSecretKey();
        log.info("Generated path: {}", path);
        return path;
    }

    private void validateFolderAccess(Long folderId, Long userId) {
        if (folderId != null) {
            UserFolder folder = folderMapper.findByIdAndUserId(folderId, userId);
            if (folder == null) {
                throw new RuntimeException("文件夹不存在或无权限访问");
            }
            // 递归验证父文件夹权限
            if (folder.getParentId() != null) {
                validateFolderAccess(folder.getParentId(), userId);
            }
        }
    }

    private void validateFileAccess(Long fileId, Long userId) {
        if (fileId != null) {
            UserFile file = fileMapper.findByIdAndUserId(fileId, userId);
            if (file == null) {
                throw new RuntimeException("文件不存在或无权限访问");
            }
            // 验证文件所属文件夹的权限
            if (file.getFolderId() != null) {
                validateFolderAccess(file.getFolderId(), userId);
            }
        }
    }

    @Override
    public UserFolder createFolder(String folderName, Long parentId, Long userId) {
        // 验证父文件夹访问权限
        validateFolderAccess(parentId, userId);
        // 确保用户目录存在
        String userPath = getUserUploadPath(userId);
        File userDir = new File(userPath);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        // 构建完整的文件夹路径
        StringBuilder folderPathBuilder = new StringBuilder(userPath);
        if (parentId != null) {
            // 递归获取父文件夹路径
            List<String> parentPaths = new ArrayList<>();
            Long currentParentId = parentId;
            while (currentParentId != null) {
                UserFolder parentFolder = folderMapper.findByIdAndUserId(currentParentId, userId);
                if (parentFolder == null) {
                    throw new RuntimeException("父文件夹不存在");
                }
                parentPaths.add(0, parentFolder.getFolderName());
                currentParentId = parentFolder.getParentId();
            }
            // 添加所有父文件夹到路径
            for (String parentPath : parentPaths) {
                folderPathBuilder.append(File.separator).append(parentPath);
            }
        }
        // 添加当前文件夹名
        folderPathBuilder.append(File.separator).append(folderName);
        
        // 创建实际的文件夹
        File folder = new File(folderPathBuilder.toString());
        if (!folder.exists()) {
            folder.mkdirs();
        }

        UserFolder userFolder = new UserFolder();
        userFolder.setFolderName(folderName);
        userFolder.setParentId(parentId);
        userFolder.setUserId(userId);
        userFolder.setCreateTime(LocalDateTime.now());
        userFolder.setUpdateTime(LocalDateTime.now());
        
        folderMapper.insert(userFolder);
        return userFolder;
    }

    @Override
    public UserFile uploadFile(MultipartFile file, Long folderId, Long userId) {
        // 验证文件夹访问权限
        validateFolderAccess(folderId, userId);
        try {
            // 获取用户专属目录
            String userPath = getUserUploadPath(userId);
            File userDir = new File(userPath);
            if (!userDir.exists()) {
                userDir.mkdirs();
            }

            // 如果指定了文件夹ID，获取文件夹路径
            String targetPath = userPath;
            String relativePath = "";
            if (folderId != null) {
                UserFolder folder = folderMapper.findByIdAndUserId(folderId, userId);
                if (folder != null) {
                    targetPath = userPath + File.separator + folder.getFolderName();
                    relativePath = folder.getFolderName() + File.separator;
                    File folderDir = new File(targetPath);
                    if (!folderDir.exists()) {
                        folderDir.mkdirs();
                    }
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 保存文件到目标目录
            File destFile = new File(targetPath + File.separator + fileName);
            file.transferTo(destFile);
            
            // 保存文件信息到数据库
            UserFile userFile = new UserFile();
            userFile.setFileName(fileName);
            userFile.setOriginalName(originalFilename);
            // 只保存相对路径，不包含用户根目录和secretKey
            userFile.setFilePath(relativePath + fileName);
            userFile.setFileSize(file.getSize());
            userFile.setFileType(file.getContentType());
            userFile.setFolderId(folderId);
            userFile.setUserId(userId);
            userFile.setCreateTime(LocalDateTime.now());
            userFile.setUpdateTime(LocalDateTime.now());
            
            fileMapper.insert(userFile);
            return userFile;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public List<UserFolder> getFolders(Long parentId, Long userId) {
        // 验证父文件夹访问权限
        validateFolderAccess(parentId, userId);
        return folderMapper.findByUserIdAndParentId(userId, parentId);
    }

    @Override
    public List<UserFile> getFiles(Long folderId, Long userId) {
        // 验证文件夹访问权限
        validateFolderAccess(folderId, userId);
        return fileMapper.findByFolderIdAndUserId(folderId, userId);
    }

    @Override
    public UserFile getFileDetail(Long fileId, Long userId) {
        // 验证文件访问权限
        validateFileAccess(fileId, userId);
        return fileMapper.findByIdAndUserId(fileId, userId);
    }

    @Override
    public List<FolderTree> getFolderTree(Long userId) {
        // 验证根目录访问权限
        validateFolderAccess(null, userId);
        // 获取根目录下的所有文件夹
        List<UserFolder> rootFolders = folderMapper.findByUserIdAndParentId(userId, null);
        return buildFolderTree(rootFolders, userId);
    }

    private List<FolderTree> buildFolderTree(List<UserFolder> folders, Long userId) {
        List<FolderTree> tree = new ArrayList<>();
        for (UserFolder folder : folders) {
            FolderTree node = new FolderTree();
            node.setId(folder.getId());
            node.setFolderName(folder.getFolderName());
            
            // 获取子文件夹
            List<UserFolder> children = folderMapper.findByUserIdAndParentId(userId, folder.getId());
            if (!children.isEmpty()) {
                node.setChildren(buildFolderTree(children, userId));
            }
            
            // 获取当前文件夹下的文件
            List<UserFile> files = fileMapper.findByFolderIdAndUserId(folder.getId(), userId);
            node.setFiles(files);
            
            tree.add(node);
        }
        return tree;
    }

    @Override
    public void deleteFile(Long fileId, Long userId) {
        // 验证文件访问权限
        validateFileAccess(fileId, userId);
        
        // 获取文件信息
        UserFile file = fileMapper.findByIdAndUserId(fileId, userId);
        if (file == null) {
            throw new RuntimeException("文件不存在或无权限访问");
        }

        // 删除物理文件
        String userPath = getUserUploadPath(userId);
        File physicalFile = new File(userPath + File.separator + file.getFilePath());
        if (physicalFile.exists()) {
            physicalFile.delete();
        }

        // 删除数据库记录
        fileMapper.deleteById(fileId);
    }

    @Override
    public void deleteFolder(Long folderId, Long userId) {
        // 验证文件夹访问权限
        validateFolderAccess(folderId, userId);
        
        // 获取文件夹信息
        UserFolder folder = folderMapper.findByIdAndUserId(folderId, userId);
        if (folder == null) {
            throw new RuntimeException("文件夹不存在或无权限访问");
        }

        // 获取用户根目录
        String userPath = getUserUploadPath(userId);
        
        // 递归删除子文件夹
        List<UserFolder> subFolders = folderMapper.findByUserIdAndParentId(userId, folderId);
        for (UserFolder subFolder : subFolders) {
            deleteFolder(subFolder.getId(), userId);
        }

        // 删除文件夹下的所有文件
        List<UserFile> files = fileMapper.findByFolderIdAndUserId(folderId, userId);
        for (UserFile file : files) {
            // 删除物理文件
            File physicalFile = new File(userPath + File.separator + file.getFilePath());
            if (physicalFile.exists()) {
                physicalFile.delete();
            }
            // 删除数据库记录
            fileMapper.deleteById(file.getId());
        }

        // 构建完整的文件夹路径
        StringBuilder folderPathBuilder = new StringBuilder(userPath);
        if (folder.getParentId() != null) {
            // 递归获取父文件夹路径
            List<String> parentPaths = new ArrayList<>();
            Long currentParentId = folder.getParentId();
            while (currentParentId != null) {
                UserFolder parentFolder = folderMapper.findByIdAndUserId(currentParentId, userId);
                if (parentFolder == null) {
                    throw new RuntimeException("父文件夹不存在");
                }
                parentPaths.add(0, parentFolder.getFolderName());
                currentParentId = parentFolder.getParentId();
            }
            // 添加所有父文件夹到路径
            for (String parentPath : parentPaths) {
                folderPathBuilder.append(File.separator).append(parentPath);
            }
        }
        // 添加当前文件夹名
        folderPathBuilder.append(File.separator).append(folder.getFolderName());

        // 删除物理文件夹
        File physicalFolder = new File(folderPathBuilder.toString());
        if (physicalFolder.exists()) {
            physicalFolder.delete();
        }

        // 删除数据库记录
        folderMapper.deleteById(folderId);
    }
} 