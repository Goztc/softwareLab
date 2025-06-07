// src/api/file.ts
import request from '@/utils/request'
import type { MyFile, ApiResponse } from '@/types'

export const fileApi = {
    /**
     * 文件上传
     * @param folderId 目标文件夹ID
     * @param file 文件对象
     */
    upload: (folderId: number, file: File): Promise<ApiResponse<MyFile>> => {
        const formData = new FormData();
        formData.append('file', file); // 确保这里的 file 是原生 File 对象

        return request.post(`/files/upload?folderId=${folderId}`, formData);
    },

    /**
     * 创建文本文件
     * @param content 文件内容
     * @param fileName 文件名
     * @param folderId 文件夹 Id
     */
    createText: (content: string, fileName: string, folderId: number): Promise<ApiResponse<MyFile>> => {
        return request.post('/files/create-text', {
            content: content,
            fileName: fileName,
            folderId: folderId
        })
    },

    /**
     * 文件重命名
     * @param fileId 文件ID
     * @param newName 新文件名
     */
    rename: (fileId: number, newName: string): Promise<ApiResponse<MyFile>> => {
        return request.put(`/files/${fileId}/rename`, null, {
            params: { newName }
        })
    },

    /**
     * 移动文件
     * @param fileId 文件ID
     * @param targetFolderId 目标文件夹ID
     */
    move: (fileId: number, targetFolderId: number): Promise<ApiResponse> => {
        return request.put(`/files/${fileId}/move`, null, {
            params: { targetFolderId }
        })
    },

    /**
     * 文件下载（处理为Blob）
     * @param fileId 文件ID
     */
    download: async (fileId: number): Promise<Blob> => {
        const response = await request.get(`/files/${fileId}/download`, {
            responseType: 'blob'
        })
        return response.data
    },

    /**
     * 删除文件
     * @param fileId 文件ID
     */
    delete: (fileId: number): Promise<ApiResponse> => {
        return request.delete(`/files/${fileId}`)
    },

    /**
     * 获取文件内容
     * @param fileId 文件ID
     */
    getContent: (fileId: number): Promise<ApiResponse<string>> => {
        if (!Number.isInteger(fileId) || fileId <= 0) {
            return Promise.reject(new Error('文件 ID 必须是正整数'));
        }
        return request.get(`/files/${fileId}/content`);
    },


    /**
     * 获取文件元数据
     * @param fileId 文件ID
     */
    getMetadata: (fileId: number): Promise<ApiResponse<MyFile>> => {
        return request.get(`/files/${fileId}`)
    }
}