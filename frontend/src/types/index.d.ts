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
    sources?: DocumentSource[]  // RAG文档引用信息
}

// RAG文档引用类型
export interface DocumentSource {
    source: string
    content: string
    absolute_path?: string
}

export interface MyFile {
    id: number
    userId: number
    folderId: number
    fileName: string
    uploadTime: string
}

export interface Folder {
    id: number
    parentId: number
    userId: number
    folderName: string
    createTime: string
    updateTime: string
    children: Folder[]
}

export interface FolderContents {
    files: MyFile[]
    folders: Folder[]
}

export interface ApiResponse<T = any> {
    code: number
    message: string
    data: T
}

