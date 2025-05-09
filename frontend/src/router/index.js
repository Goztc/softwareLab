// 从 vue-router 库中导入 createRouter 和 createWebHistory 函数
// createRouter 用于创建一个新的路由实例
// createWebHistory 用于创建基于 HTML5 History API 的路由历史记录
import { createRouter, createWebHistory } from 'vue-router';

// 导入组件
// 导入 Home.vue 组件，该组件通常作为主页显示
import HomeVue from '@/components/Home.vue';
// 导入 gpt.vue 组件，该组件可能用于与 GPT 相关的聊天功能
import ChatVue from '@/components/gpt.vue';
import LoginVue from '@/views/Login.vue';
import UserVue from '@/components/user.vue';
// 定义路由配置
// routes 是一个数组，包含多个路由对象，每个对象代表一个路由规则
const routes = [
    { path: '/login', component: LoginVue },
    {
        path: '/', component: HomeVue, redirect: '/home', children: [
            { path: '/home', component: UserVue },
            { path: '/chat', component: ChatVue },
        ]
        // { path: '/', component: LayoutVue, ... }: 这是另一个路由对象，它定义了根路径 / 的路由。这里使用的 LayoutVue 组件通常是一个包含应用布局的主组件。
        // redirect: '/article/manage': 这表示当用户访问根路径 / 时，应用将会自动重定向到 /article/manage 路径。
        // children: [: 这是一个子路由数组，表示嵌套路由。这些子路由的路径是相对于父路径 / 的。
    }

];


// 创建路由器
// 使用 createRouter 函数创建一个新的路由实例
// history 选项指定路由的历史记录模式，这里使用 HTML5 History API
// routes 选项指定路由配置，使用了对象属性简写语法
const router = createRouter({
    history: createWebHistory(),
    routes, // 使用简写
});

// 导出路由器
// 将创建好的路由实例导出，以便在其他文件中使用
export default router;
