// @/stores/chat.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatSession, ChatMessage } from '@/types'
import { createSession, sendMessage, getMessageHistory, getSessions } from '@/api/chat'
import { marked } from 'marked'

export const useChatStore = defineStore('chat', () => {
    const currentUserId = ref<number>(1)
    const sessions = ref<ChatSession[]>([])
    const currentSessionId = ref<number | null>(null)
    const messages = ref<ChatMessage[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    // 统一错误处理函数
    const handleError = (err: unknown, defaultMessage: string) => {
        error.value = err instanceof Error ? err.message : defaultMessage
        console.error(error.value, err)
        throw err
    }

    // 创建新会话
    const createNewSession = async (sessionName: string) => {
        loading.value = true
        error.value = null
        try {
            const { code, data, message } = await createSession(sessionName)
            if (code !== 0) throw new Error(message)

            sessions.value.unshift(data)
            currentSessionId.value = data.id
            messages.value = []
            return data
        } catch (err) {
            handleError(err, '创建会话失败')
        } finally {
            loading.value = false
        }
    }

    // 发送消息
    const sendNewMessage = async (content: string) => {
        if (!currentSessionId.value) {
            throw new Error('No active session')
        }

        loading.value = true
        error.value = null

        try {
            // 用户消息
            const userMessage: ChatMessage = {
                id: Date.now(),
                sessionId: currentSessionId.value,
                userId: currentUserId.value,
                content,
                role: 'user',
                createTime: new Date().toISOString()
            }
            messages.value.push(userMessage)

            // 发送API请求
            const { code, data, message } = await sendMessage(
                currentSessionId.value,
                content
            )
            if (code !== 0) throw new Error(message)

            // AI消息
            const aiMessage: ChatMessage = {
                id: Date.now() + 1,
                sessionId: currentSessionId.value,
                userId: 0,
                // content: await marked.parse(data.content || ''),
                content: data.content || '',
                role: 'assistant',
                createTime: new Date().toISOString()
            }
            messages.value.push(aiMessage)
            return aiMessage
        } catch (err) {
            // 发送失败时移除用户消息
            messages.value = messages.value.filter(msg => msg.role !== 'user' || msg.content !== content)
            handleError(err, '发送消息失败')
        } finally {
            loading.value = false
        }
    }

    // 获取消息历史
    const loadMessageHistory = async (sessionId: number) => {
        loading.value = true
        error.value = null
        try {
            const { code, data, message } = await getMessageHistory(sessionId)
            if (code !== 0) throw new Error(message)

            // 并行处理Markdown解析
            // messages.value = await Promise.all(
            //     data.map(async msg => ({
            //         ...msg,
            //         content: msg.role === 'assistant'
            //             ? await marked.parse(msg.content)
            //             : msg.content
            //     }))
            // )
            messages.value = data.map(msg => ({
                ...msg,
                content: msg.content // 原始内容，由前端组件解析
            }))
            currentSessionId.value = sessionId
        } catch (err) {
            handleError(err, '加载消息历史失败')
        } finally {
            loading.value = false
        }
    }

    // 加载会话列表
    const loadSessions = async () => {
        loading.value = true
        error.value = null
        try {
            const { code, data, message } = await getSessions()
            if (code !== 0) throw new Error(message)
            sessions.value = data || []
        } catch (err) {
            handleError(err, '加载会话列表失败')
        } finally {
            loading.value = false
        }
    }

    return {
        currentUserId,
        sessions,
        currentSessionId,
        messages,
        loading,
        error,
        createNewSession,
        sendNewMessage,
        loadMessageHistory,
        loadSessions
    }
})