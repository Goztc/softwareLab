// 导入 axios 库，它是一个基于 Promise 的 HTTP 客户端，用于浏览器和 Node.js
import axios from 'axios';
// 从 element-plus 库中导入 ElMessage 组件，用于在页面上显示消息提示
import { ElMessage } from 'element-plus';

// 定义一个变量，记录公共的前缀 baseURL，所有请求都会基于这个前缀发起
// 这里设置为 '/api'，表示所有请求的 URL 都会自动加上 '/api' 前缀
const baseURL = '/api';

// 创建 axios 实例，通过 axios.create 方法可以创建一个自定义的 axios 实例
// 可以为这个实例单独配置一些参数，这里配置了 baseURL
const instance = axios.create({ baseURL });

import { useTokenStore } from '@/stores/token.js'
//添加 Axios 请求拦截器 ，用于在请求发送之前执行某些操作
instance.interceptors.request.use(
    (config) => {
        // 第一个参数是一个回调函数，它在请求发送之前被调用。这个函数接受一个 config 对象作为参数，这个对象包含了请求的所有配置。
        //请求前的回调
        //添加token
        const tokenStore = useTokenStore();
        // 检查 tokenStore 是否存在 token。
        if (tokenStore.token) {
            // 如果存在 token，就将它添加到请求头的 Authorization 字段中。这通常用于 API 身份验证。
            config.headers.Authorization = tokenStore.token
        }
        return config;
        // 修改后的配置被返回给 Axios，随后请求将继续进行。
    },
    (err) => {
        //请求错误的回调
        // 如果请求配置过程中发生了错误，它会将错误通过 Promise.reject(err) 传递出去，以便后续的错误处理可以捕获到这个错误。
        Promise.reject(err)
    }
)
/*
这段代码的主要目的是在每次发送请求之前，检查用户是否已经登录（通过 token），如果是，就将 token 添加到请求头中，
以便进行身份验证。当请求发生错误时，也会正常处理错误。这种做法常见于需要认证的 API 请求场景。
*/

// 为 axios 实例添加响应拦截器，响应拦截器可以在请求返回后对响应数据进行处理
// 第一个参数是成功响应的处理函数，第二个参数是错误响应的处理函数
instance.interceptors.response.use(
    // 成功响应的处理函数，当服务器返回状态码为 2xx 时会执行此函数
    response => {
        // 判断业务状态码，假设服务器返回的数据结构中包含 code 字段
        // 当 code 为 0 时，表示业务处理成功
        if (response.data.code === 0) {
            // 业务处理成功，直接返回响应数据中的 data 部分
            return response.data;
        }

        // 当业务状态码不为 0 时，表示操作失败
        // 使用 ElMessage 组件显示错误信息，优先使用服务器返回的 msg 字段
        // 如果 msg 字段不存在，则显示默认的错误信息 '服务异常'
        ElMessage.error(response.data.msg || '服务异常');
        // 返回一个被拒绝的 Promise，将错误信息传递给调用者
        return Promise.reject(response.data);
    },
    // 错误响应的处理函数，当请求过程中出现错误（如网络错误、超时等）时会执行此函数
    error => {
        // 使用 ElMessage 组件显示错误信息，优先使用错误对象的 message 字段
        // 如果 message 字段不存在，则显示默认的错误信息 '网络异常'
        ElMessage.error(error.message || '网络异常');
        // 返回一个被拒绝的 Promise，将错误对象传递给调用者
        return Promise.reject(error);
    }
);
// 在 @/utils/request.js 中，你只暴露了一个默认导出（export default instance），这意味着：
// 这个文件 ​只导出一个值​（instance）。
// 其他文件在导入时，可以​自定义导入名称​（比如 request），而不必使用原变量名 instance。
// 导出创建好的 axios 实例，其他模块可以通过 import 引入并使用这个实例发起请求
export default instance;
