import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import chatApi from '@/api/chat.js'

export const useChatStore = defineStore('chat', () => {
    // ==================== 状态定义 ====================
    /** @type {import('vue').Ref<ChatMessage[]>} 消息列表 */
    const messages = ref([])

    /** @type {import('vue').Ref<string>} 当前会话ID */
    const currentSessionId = ref('')

    /** @type {import('vue').Ref<ChatSession[]>} 会话历史 */
    const sessions = ref([])

    /** @type {import('vue').Ref<boolean>} 是否正在加载 */
    const isLoading = ref(false)

    // ==================== 计算属性 ====================
    /** 当前会话消息 */
    const currentMessages = computed(() =>
        messages.value.filter(msg => msg.sessionId === currentSessionId.value)
    )

    /** 当前会话详情 */
    const currentSession = computed(() =>
        sessions.value.find(s => s.id === currentSessionId.value)
    )

    // ==================== 方法定义 ====================
    /**
     * 加载会话历史
     * @async
     * @returns {Promise<void>}
     */
    const loadSessions = async () => {
        isLoading.value = true
        try {
            const res = await chatApi.getSessionHistory()
            sessions.value = res.data
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 创建新会话
     * @async
     * @param {string} [title] - 会话标题
     * @returns {Promise<ChatSession>}
     */
    const createSession = async (title) => {
        isLoading.value = true
        try {
            const res = await chatApi.createSession({ title })
            sessions.value.unshift(res.data)
            currentSessionId.value = res.data.id
            return res.data
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 发送消息
     * @async
     * @param {string} content - 消息内容
     * @returns {Promise<ChatMessage>}
     */
    const sendMessage = async (content) => {
        if (!currentSessionId.value) {
            await createSession()
        }

        // 先添加本地临时消息
        const tempMsg = addLocalMessage({
            sessionId: currentSessionId.value,
            sender: 'user',
            type: 'text',
            content,
            status: 'sending'
        })

        try {
            // 调用API发送
            const res = await chatApi.sendMessage({
                sessionId: currentSessionId.value,
                content
            })

            // 更新消息状态
            updateMessageStatus(tempMsg.id, 'delivered', res.data)
            return res.data
        } catch (error) {
            updateMessageStatus(tempMsg.id, 'failed')
            throw error
        }
    }

    /**
     * 添加本地消息 (不调用API)
     * @param {Omit<ChatMessage, 'id' | 'timestamp'>} message
     * @returns {ChatMessage}
     */
    const addLocalMessage = (message) => {
        const newMsg = {
            ...message,
            id: generateId(),
            timestamp: Date.now()
        }
        messages.value.push(newMsg)
        return newMsg
    }

    /**
     * 更新消息状态
     * @param {string} messageId 
     * @param {MessageStatus} status 
     * @param {Object} [data] - 更新数据
     */
    const updateMessageStatus = (messageId, status, data) => {
        const index = messages.value.findIndex(m => m.id === messageId)
        if (index !== -1) {
            messages.value[index] = {
                ...messages.value[index],
                status,
                ...data
            }
        }
    }

    // ==================== 工具函数 ====================
    /** 生成唯一ID */
    const generateId = () => {
        return Date.now().toString(36) + Math.random().toString(36).substring(2)
    }

    return {
        // 状态
        messages,
        currentSessionId,
        sessions,
        isLoading,

        // 计算属性
        currentMessages,
        currentSession,

        // 方法
        loadSessions,
        createSession,
        sendMessage,
        addLocalMessage,
        updateMessageStatus
    }
}, {
    persist: {
        key: 'chat-storage',
        paths: ['messages', 'currentSessionId', 'sessions'],
        storage: localStorage,
        serializer: {
            serialize: JSON.stringify,
            deserialize: JSON.parse
        }
    }
})