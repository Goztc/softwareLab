// src/stores/file.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Folder, FolderContents } from '@/types'
import { fileApi } from '@/api/file'
import { folderApi } from '@/api/folder'

export const useFileStore = defineStore('file', () => {
    // 当前所在文件夹ID（0 表示根目录）
    const currentFolderId = ref<number>(0)
    // 当前文件夹内容
    const currentFolderContents = ref<FolderContents>({ files: [], folders: [] })
    // 整个文件夹树
    const folderTree = ref<Folder[]>([])
    // 加载状态
    const loading = ref(false)
    const error = ref<string | null>(null)

    // 获取当前文件夹路径（可根据需要实现）
    // 获取当前文件夹路径
    const currentFolderPath = computed(() => {
        if (currentFolderId.value === 0) {
            return [{ id: 0, folderName: '根目录' }]
        }

        // 在文件夹树中查找当前文件夹的路径
        const findPath = (tree: Folder[], targetId: number, path: Folder[] = []): Folder[] | null => {
            for (const folder of tree) {
                // 创建当前路径副本
                const currentPath = [...path, folder]

                // 如果找到目标文件夹
                if (folder.id === targetId) {
                    return currentPath
                }

                // 如果有子文件夹，递归查找
                if (folder.children && folder.children.length > 0) {
                    const foundPath = findPath(folder.children, targetId, currentPath)
                    if (foundPath) return foundPath
                }
            }
            return null
        }

        // 从根目录开始查找
        const path = findPath(folderTree.value, currentFolderId.value)

        // 如果找不到路径（可能是数据未加载），返回空数组
        return path || []
    })

    // 初始化仓库时自动加载文件夹树
    const initStore = async () => {
        try {
            loading.value = true;

            // 1. 尝试获取文件夹树
            const { code: treeCode, data: treeData, message: treeMsg } = await folderApi.getFolderTree();

            // 2. 如果获取失败或树为空，初始化根目录
            if (treeCode !== 0 || !treeData?.length) {
                const { code: initCode, data: initData, message: initMsg } = await folderApi.initRootFolder();
                if (initCode !== 0) throw new Error(`初始化根目录失败: ${initMsg}`);

                // 更新为新建的根目录数据
                folderTree.value = [initData];
                currentFolderId.value = initData.id;
            } else {
                // 3. 正常情况使用获取到的树数据
                folderTree.value = treeData;

                // 自动选择第一个文件夹（或保持当前选择）
                if (!currentFolderId.value) {
                    currentFolderId.value = treeData[0].id;
                }
            }

            // 4. 加载当前文件夹内容（带兜底逻辑）
            await loadFolderContents(currentFolderId.value || null);

        } catch (err) {
            error.value = `初始化失败: ${err instanceof Error ? err.message : String(err)}`;
            // 可添加重试逻辑或回退UI
        } finally {
            loading.value = false;
        }
    };

    // 加载指定文件夹内容
    const loadFolderContents = async (folderId: number) => {
        try {
            loading.value = true
            const { code, data, message } = await folderApi.getContents(folderId)
            if (code !== 0) throw new Error(message)
            currentFolderContents.value = data
            currentFolderId.value = folderId
        } catch (err) {
            error.value = '加载文件夹内容失败'
        } finally {
            loading.value = false
        }
    }

    // 创建文件夹
    const createFolder = async (name: string) => {
        try {
            const { code, data, message } = await folderApi.createFolder(currentFolderId.value, name)
            if (code !== 0) throw new Error(message)
            currentFolderContents.value.folders.push(data)
            updateFolderTree(data) // 更新文件夹树
        } catch (err) {
            error.value = '创建文件夹失败'
        }
    }

    // 更新文件夹树（递归查找父节点并添加）
    const updateFolderTree = (newFolder: Folder, tree = folderTree.value) => {
        for (const folder of tree) {
            if (folder.id === newFolder.parentId) {
                folder.children.push(newFolder)
                return true
            }
            if (folder.children && updateFolderTree(newFolder, folder.children)) {
                return true
            }
        }
        return false
    }

    // 上传文件
    const uploadFile = async (file: File) => {
        try {
            loading.value = true;
            error.value = null;

            const { code, data, message } = await fileApi.upload(currentFolderId.value, file);
            if (code !== 0) throw new Error(message);

            currentFolderContents.value.files.push(data);
            return data; // Return the uploaded file for potential use
        } catch (err) {
            error.value = `文件上传失败: ${err instanceof Error ? err.message : String(err)}`;
            throw err;
        } finally {
            loading.value = false;
        }
    };

    // 删除文件
    const deleteFile = async (fileId: number) => {
        try {
            await fileApi.delete(fileId)
            currentFolderContents.value.files = currentFolderContents.value.files.filter(
                f => f.id !== fileId
            )
        } catch (err) {
            error.value = '文件删除失败'
        }
    }

    // 删除文件夹
    const deleteFolder = async (folderId: number) => {
        try {
            await folderApi.deleteFolder(folderId)
            currentFolderContents.value.folders = currentFolderContents.value.folders.filter(
                f => f.id !== folderId
            )
            removeFromFolderTree(folderId) // 更新文件夹树
        } catch (err) {
            error.value = '文件夹删除失败'
        }
    }

    // 从文件夹树中移除
    const removeFromFolderTree = (folderId: number, tree = folderTree.value) => {
        for (let i = 0; i < tree.length; i++) {
            if (tree[i].id === folderId) {
                tree.splice(i, 1)
                return true
            }
            if (
                tree[i].children &&
                removeFromFolderTree(folderId, tree[i].children)
            ) {
                return true
            }
        }
        return false
    }

    // 重命名文件或文件夹
    const renameItem = async (payload: {
        id: number
        newName: string
        isFolder: boolean
    }) => {
        try {
            if (payload.isFolder) {
                const { code, data, message } = await folderApi.renameFolder(payload.id, payload.newName)
                if (code !== 0) throw new Error(message)
                updateFolderNameInTree(data)
            } else {
                const { code, data, message } = await fileApi.rename(payload.id, payload.newName)
                if (code !== 0) throw new Error(message)
                currentFolderContents.value.files = currentFolderContents.value.files.map(
                    f => (f.id === data.id ? data : f)
                )
            }
        } catch (err) {
            error.value = '重命名失败'
        }
    }

    // 更新文件夹树中的名称
    const updateFolderNameInTree = (updatedFolder: Folder, tree = folderTree.value) => {
        for (const folder of tree) {
            if (folder.id === updatedFolder.id) {
                folder.folderName = updatedFolder.folderName
                return true
            }
            if (folder.children && updateFolderNameInTree(updatedFolder, folder.children)) {
                return true
            }
        }
        return false
    }

    return {
        currentFolderId,
        currentFolderContents,
        folderTree,
        loading,
        error,
        currentFolderPath,

        initStore,
        loadFolderContents,
        createFolder,
        uploadFile,
        deleteFile,
        deleteFolder,
        renameItem,
    }
})