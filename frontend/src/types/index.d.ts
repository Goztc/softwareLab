// src/types/index.d.ts
export interface User {
    id: number
    username: string
    password: string
    createTime: string
    avatar?: string
}

export interface ChatSession {
    id: number
    userId: number
    sessionName: string
    createTime: string
    updateTime: string
    lastMessage?: string
}

export interface ChatMessage {
    id: number
    sessionId: number
    userId: number
    content: string
    role: 'user' | 'assistant'
    createTime: string
}

export interface ApiResponse<T = any> {
    code: number
    message: string
    data: T
}

