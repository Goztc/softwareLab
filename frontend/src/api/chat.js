import request from '@/utils/request'

/**
 * 聊天API服务
 * 封装所有与聊天相关的API请求
 */

export default {
    /**
     * 发送消息
     * @param {Object} params
     * @param {string} params.sessionId
     * @param {string} params.content
     * @returns {Promise<ApiResponse<ChatMessage>>}
     */
    async sendMessage(params) {
        return request.post('/chat/messages', params)
    },

    /**
     * 创建会话
     * @param {Object} [config]
     * @param {string} [config.title]
     * @returns {Promise<ApiResponse<ChatSession>>}
     */
    async createSession(config) {
        return request.post('/chat/sessions', config)
    },

    /**
     * 获取会话历史
     * @returns {Promise<ApiResponse<ChatSession[]>>}
     */
    async getSessionHistory() {
        return request.get('/chat/sessions')
    },

    /**
     * 获取会话消息
     * @param {string} sessionId
     * @returns {Promise<ApiResponse<ChatMessage[]>>}
     */
    async getSessionMessages(sessionId) {
        return request.get(`/chat/sessions/${sessionId}/messages`)
    },

    /**
     * 重置会话
     * @param {string} sessionId
     * @returns {Promise<ApiResponse>}
     */
    async resetSession(sessionId) {
        return request.post(`/chat/sessions/${sessionId}/reset`)
    }
}