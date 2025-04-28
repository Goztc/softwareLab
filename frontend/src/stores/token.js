//定义store
import { defineStore } from 'pinia'
import { ref } from 'vue'
/* 
    第一个参数: 名字,唯一性
    第二个参数: 函数,函数的内部可以定义状态的所有内容

    返回值: 函数
*/
// 这段代码使用的是 Pinia，一个用于 Vue.js 应用程序的状态管理库
// 定义并导出一个名为 useTokenStore 的 store。这是一个用于管理与 token 相关状态的 Pinia 存储。
// defineStore('token', () => {...}): defineStore 是 Pinia 提供的函数，用于创建一个新的 store，第一个参数 'token' 是 store 的唯一标识符。
export const useTokenStore = defineStore('token', () => {
    //定义状态的内容

    //1.响应式变量
    // const token = ref(''): ref 是 Vue.js 中的响应式引用，用于创建一个响应式变量 token，初始值为空字符串。
    // 响应式变量这意味着应用中任何使用这个 token 的组件都会在 token 更新时自动重新渲染。
    const token = ref('')

    //2.定义一个函数, 修改token的值
    const setToken = (newToken) => {
        token.value = newToken
    }

    //3.函数, 移除token的值
    const removeToken = () => {
        token.value = ''
    }

    return {
        token, setToken, removeToken
    }
    // return {...}: 将 token、setToken 和 removeToken 函数返回，使得它们可以在组件中被访问。
}, {
    persist: true //持久化存储
    // persist: true: 此选项使 Pinia 存储的状态在页面刷新后仍然保持。这意味着如果用户设置了 token，它将在浏览器的本地存储中持久存在，能够在用户刷新页面时恢复。
});

// 在其他地方的使用示例
// const tokenStore = useTokenStore();
// if(tokenStore.token){
//     config.headers.Authorization = tokenStore.token
// }