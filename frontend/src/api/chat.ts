// src/api/chat.ts
import request from '@/utils/request.js';
import type { ChatSession, ChatMessage, ApiResponse } from '@/types';

/**
 * 功能描述：创建聊天会话
 * @param userId - 用户ID
 * @param sessionName - 会话名称
 * @returns Promise<ApiResponse<ChatSession>>
 */
export function createSession(sessionName: string): Promise<ApiResponse<ChatSession>> {
    return request.post('/chat/sessions', null, {
        params: { sessionName },
    });
}

/**
 * 功能描述：发送消息
 * @param sessionId - 会话ID
 * @param userId - 用户ID
 * @param content - 消息内容
 * @returns Promise<ApiResponse<ChatMessage>>
 */
export function sendMessage(
    sessionId: number,
    content: string
): Promise<ApiResponse<ChatMessage>> {
    return request.post('/chat/messages',
        { "content": content },
        {
            params: { sessionId },
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );
}

/**
 * 功能描述：获取消息历史
 * @param sessionId - 会话ID
 * @returns Promise<ApiResponse<ChatMessage[]>>
 */
export function getMessageHistory(
    sessionId: number
): Promise<ApiResponse<ChatMessage[]>> {
    return request.get('/chat/messages', {
        params: { sessionId },
    });
}

/**
 * 功能描述：获取会话列表
 * @returns Promise<ApiResponse<ChatSession[]>> 返回会话列表的Promise
 */
export function getSessions(): Promise<ApiResponse<ChatSession[]>> {
    return request.get('/chat/sessions');
}

/**
 * 功能描述：重命名会话
 * @param sessionId - 会话ID
 * @param sessionName - 新的会话名称
 * @returns Promise<ApiResponse<ChatSession>>
 */
export function renameSession(
    sessionId: number,
    sessionName: string
): Promise<ApiResponse<ChatSession>> {
    return request.put(`/chat/sessions/${sessionId}`, 
        { sessionName },
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );
}

/**
 * 功能描述：删除会话
 * @param sessionId - 会话ID
 * @returns Promise<ApiResponse<void>>
 */
export function deleteSession(sessionId: number): Promise<ApiResponse<void>> {
    return request.delete(`/chat/sessions/${sessionId}`);
}

/**
 * 功能描述：清除会话历史
 * @param sessionId - 会话ID
 * @returns Promise<ApiResponse<void>>
 */
export function clearSessionHistory(sessionId: number): Promise<ApiResponse<void>> {
    return request.delete(`/chat/sessions/${sessionId}/history`);
}

/**
 * 功能描述：发送带文件选择的消息
 * @param sessionId - 会话ID
 * @param messageData - 消息数据（包含内容、文件ID和文件夹ID）
 * @returns Promise<ApiResponse<ChatMessage>>
 */
export function sendMessageWithFiles(
    sessionId: number,
    messageData: {
        content: string;
        fileIds?: number[];
        folderIds?: number[];
    }
): Promise<ApiResponse<ChatMessage>> {
    return request.post('/chat/messages',
        messageData,
        {
            params: { sessionId },
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );
}

/**
 * 功能描述：获取用户文件夹和文件结构
 * @returns Promise<ApiResponse<{folders: any[], files: any[]}>>
 */
export function getFolders(): Promise<ApiResponse<{folders: any[], files: any[]}>> {
    return request.get('/chat/folders');
}

// 导出chatApi对象以便在其他地方使用
export const chatApi = {
    createSession,
    sendMessage,
    sendMessageWithFiles,
    getMessageHistory,
    getSessions,
    renameSession,
    deleteSession,
    clearSessionHistory,
    getFolders
};
