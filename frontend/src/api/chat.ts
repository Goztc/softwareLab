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

// RAG相关API
/**
 * 功能描述：RAG聊天 - 基于文档的智能对话
 * @param message - 用户消息
 * @param documentPath - 文档路径
 * @param history - 对话历史（可选）
 * @param conversationId - 对话ID（可选）
 * @returns Promise包含回答和文档引用信息
 */
export function ragChat(
    message: string,
    documentPath: string | string[],
    history?: any[],
    conversationId?: string
): Promise<any> {
    return request.post('http://localhost:5000/chat', {
        message,
        document_path: documentPath,
        history: history || [],
        conversation_id: conversationId || 'default'
    }, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
}
