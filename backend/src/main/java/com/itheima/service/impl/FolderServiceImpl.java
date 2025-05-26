// FolderServiceImpl.java
package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mapper.FileMapper;
import com.itheima.mapper.FolderMapper;
import com.itheima.pojo.File;
import com.itheima.pojo.Folder;
import com.itheima.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService {
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public Folder createFolder(Long userId, Long parentId, String folderName) {
        // 参数校验和父文件夹归属验证
        validateParentFolder(userId, parentId);

        Folder folder = Folder.builder()
                .userId(userId)
                .parentId(parentId)
                .folderName(folderName.trim())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        baseMapper.insert(folder);
        return folder;
    }

    @Override
    public List<Folder> getFolderTree(Long userId) {
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, 0);
        List<Folder> rootFolders = baseMapper.selectList(queryWrapper);

        rootFolders.forEach(folder ->
                folder.setChildren(recursiveSubFolders(folder.getId()))
        );
        return rootFolders;
    }

    @Override
    @Transactional
    public void deleteFolderWithContents(Long userId, Long folderId) {
        // 权限验证和子文件夹获取
        validateFolderOwnership(userId, folderId);
        List<Long> subFolderIds = collectSubFolderIds(userId, folderId);

        // 先删除所有相关文件（包括子文件夹中的文件）
        fileMapper.delete(new LambdaUpdateWrapper<File>()
                .in(File::getFolderId,
                        Stream.concat(subFolderIds.stream(), Stream.of(folderId))
                                .collect(Collectors.toList()))
                .eq(File::getUserId, userId));

        // 然后删除子文件夹
        if (!subFolderIds.isEmpty()) {
            baseMapper.deleteBatchIds(subFolderIds);
        }

        // 最后删除当前文件夹
        baseMapper.deleteById(folderId);
    }

    @Override
    @Transactional
    public Folder renameFolder(Long userId, Long folderId, String newName) {
        Folder folder = validateFolderOwnership(userId, folderId);
        folder.setFolderName(newName);
        baseMapper.updateById(folder);
        return folder;
    }

    @Override
    public Map<String, Object> getFolderContents(Long userId, Long folderId) {
        validateFolderOwnership(userId, folderId);

        LambdaQueryWrapper<Folder> folderQuery = new LambdaQueryWrapper<>();
        folderQuery.eq(Folder::getParentId, folderId)
                .eq(Folder::getUserId, userId);
        List<Folder> subFolders = baseMapper.selectList(folderQuery);

        LambdaQueryWrapper<File> fileQuery = new LambdaQueryWrapper<>();
        fileQuery.eq(File::getFolderId, folderId)
                .eq(File::getUserId, userId);
        List<File> files = fileMapper.selectList(fileQuery);

        return Map.of("folders", subFolders, "files", files);
    }

    // 实现细节方法
    private List<Folder> recursiveSubFolders(Long parentId) {
        LambdaQueryWrapper<Folder> query = new LambdaQueryWrapper<>();
        query.eq(Folder::getParentId, parentId);
        List<Folder> folders = baseMapper.selectList(query);
        folders.forEach(f -> f.setChildren(recursiveSubFolders(f.getId())));
        return folders;
    }

    private List<Long> collectSubFolderIds(Long userId, Long parentId) {
        List<Long> allSubFolderIds = new ArrayList<>();
        collectSubFolderIdsRecursive(userId, parentId, allSubFolderIds);
        return allSubFolderIds;
    }

    private void collectSubFolderIdsRecursive(Long userId, Long parentId, List<Long> result) {
        LambdaQueryWrapper<Folder> query = new LambdaQueryWrapper<>();
        query.select(Folder::getId)
                .eq(Folder::getParentId, parentId)
                .eq(Folder::getUserId, userId);

        List<Long> directChildren = baseMapper.selectList(query)
                .stream()
                .map(Folder::getId)
                .toList();

        result.addAll(directChildren);

        for (Long childId : directChildren) {
            collectSubFolderIdsRecursive(userId, childId, result);
        }
    }

    private void validateParentFolder(Long userId, Long parentId) {
        if (parentId != 0 && baseMapper.selectById(parentId) == null) {
            throw new RuntimeException("父文件夹不存在");
        }
    }

    public Folder validateFolderOwnership(Long userId, Long folderId) {
        Folder folder = baseMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new RuntimeException("文件夹操作权限不足");
        }
        return folder;
    }
}