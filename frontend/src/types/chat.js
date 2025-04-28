/**
 * 聊天系统类型定义
 * 包含消息、会话等核心类型定义
 */

/**
 * 消息发送者类型
 * @typedef {'user' | 'assistant' | 'system'} MessageSender
 */

/**
 * 消息状态类型
 * @typedef {'sending' | 'sent' | 'delivered' | 'read' | 'failed'} MessageStatus
 */

/**
 * 消息内容类型
 * @typedef {'text' | 'image' | 'file' | 'audio'} MessageType
 */

/**
 * 聊天消息接口
 * @typedef {Object} ChatMessage
 * @property {string} id - 消息唯一ID
 * @property {string} sessionId - 所属会话ID
 * @property {MessageSender} sender - 发送者类型
 * @property {number} timestamp - 消息时间戳
 * @property {MessageStatus} status - 消息状态
 * @property {MessageType} type - 消息类型
 * @property {string} content - 消息内容
 * @property {Object} [metadata] - 附加元数据
 */

/**
 * 聊天会话接口
 * @typedef {Object} ChatSession
 * @property {string} id - 会话ID
 * @property {string} title - 会话标题
 * @property {number} createdAt - 创建时间戳
 * @property {number} updatedAt - 最后更新时间
 * @property {string[]} participantIds - 参与者ID数组
 * @property {Object} [settings] - 会话设置
 */

/**
 * API响应格式
 * @typedef {Object} ApiResponse
 * @property {boolean} success - 是否成功
 * @property {any} data - 响应数据
 * @property {string} [error] - 错误信息
 */

export default {}