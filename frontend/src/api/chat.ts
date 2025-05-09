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
