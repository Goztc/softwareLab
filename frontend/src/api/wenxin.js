// @/api/wenxin.js
// 此文件位于 @/api 目录下，主要用于封装与后端交互的 API 接口。
// 在 vue.config.js 中，@ 通常被设置为指向 src 目录，因此 '@/utils/request.js' 实际指向 src/utils/request.js 文件。
// 该文件依赖于 request.js，是一个封装了 axios 的工具函数，用于统一处理 HTTP 请求，如设置请求头、处理错误等。
import request from '@/utils/request.js'


export const ChatService = (question) => {
    // 将 question 对象转换为 URL 参数的字符串形式
    const params = new URLSearchParams(question).toString();
    // 发送 POST 请求到 /wenxin/ask 接口，并携带转换后的 URL 参数
    return request.post(`/wenxin/ask?${params}`);
};


export const ChatBotService = (question) => {
    // 发送 POST 请求到 /question 接口，并将 question 对象作为请求的 data 数据
    return request.post("/question", question); // 直接传递 data 对象  
};


export const ChatBotResetService = () => {
    // 发送 POST 请求到 /reset 接口，重置聊天机器人的状态
    return request.post("/reset");              // 直接传递 data 对象  
};
