// src/api/folder.ts
import request from '@/utils/request'
import type { Folder, ApiResponse, FolderContents } from '@/types'

export const folderApi = {
    /**
     * 创建文件夹
     * @param parentId - 上级文件夹ID
     * @param name - 文件夹名称
     */
    createFolder: (parentId: number, name: string): Promise<ApiResponse<Folder>> => {
        return request.post('/folders/create', null, {
            params: { parentId, folderName: name }
        })
    },

    /**
     * 获取文件夹树形结构
     */
    getFolderTree: (): Promise<ApiResponse<Folder[]>> => {
        return request.get('/folders/tree')
    },

    /**
     * 删除文件夹
     * @param folderId - 要删除的文件夹ID
     */
    deleteFolder: (folderId: number): Promise<ApiResponse> => {
        return request.delete(`/folders/${folderId}`)
    },

    /**
     * 重命名文件夹
     * @param folderId - 目标文件夹ID
     * @param newName - 新文件夹名称
     */
    renameFolder: (folderId: number, newName: string): Promise<ApiResponse<Folder>> => {
        return request.put(`/folders/${folderId}/rename`, null, {
            params: { newName }
        })
    },

    /**
     * 获取文件夹内容
     * @param folderId - 目标文件夹ID
     */
    getContents: (folderId: number): Promise<ApiResponse<FolderContents>> => {
        return request.get(`/folders/${folderId}/contents`)
    },
    /*
    {
      "code": 0,
      "message": "操作成功",
      "data": {
        "files": [
          {
            "id": 17,
            "userId": 4,
            "folderId": 18,
            "fileName": "test-document.txt",
            "uploadTime": "2025-05-26T12:45:48"
          }
        ],
        "folders": []
      }
    }
    */

    /**
     * 初始化根目录（自动创建根文件夹）
     */
    initRootFolder: (): Promise<ApiResponse<Folder>> => {
        return request.post('/folders/create', null, {
            params: { parentId: 0, folderName: '我的云盘' }
        })
    }
}