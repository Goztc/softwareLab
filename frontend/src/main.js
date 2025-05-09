// 导入项目的主样式文件，main.scss 通常包含项目的全局样式
import './assets/main.scss'

// 从 vue 库中导入 createApp 函数，用于创建一个新的 Vue 应用实例
import { createApp } from 'vue'
// 导入 ElementPlus 组件库，它是一个基于 Vue 3 的桌面端组件库，可帮助快速搭建美观的界面
import ElementPlus from 'element-plus'
// 导入 ElementPlus 的样式文件，确保组件能正确显示样式
import 'element-plus/dist/index.css'
// 从 @/router 路径导入路由配置，@ 通常是在项目配置中设置的别名，指向 src 目录
// 路由用于管理页面的导航，根据不同的 URL 显示不同的组件
import router from '@/router'
// 导入根组件 App.vue，它是整个 Vue 应用的入口组件
import App from './App.vue'
// 从 pinia 库中导入 createPinia 函数，Pinia 是 Vue 3 的状态管理库，用于在组件间共享数据
import { createPinia } from 'pinia'
// 从 @/store 路径导入状态管理实例，可能是自定义的状态管理逻辑
import store from '@/store'
// 从 pinia-persistedstate-plugin 库中导入 createPersistedState 函数
// 该插件用于将 Pinia 中的状态持久化存储到本地存储（如 localStorage）中，刷新页面后状态不会丢失
import { createPersistedState } from 'pinia-persistedstate-plugin'
// 导入 ElementPlus 的中文语言包，用于将组件的提示信息等内容显示为中文
import locale from 'element-plus/dist/locale/zh-cn.js'

// 导入项目所需的各种样式文件
import '@/assets/css/animation.css' // 动画相关的样式
import '@/assets/css/index.css' // 项目的基础样式
import '@/assets/css/tocbot.css' // tocbot 库的样式，tocbot 通常用于生成目录导航
import '@/assets/css/color.css' // 颜色相关的样式
import '@/assets/css/markdown-highlight.css' // Markdown 代码高亮的样式
import '@/assets/css/font-awesome.min.css' // Font Awesome 图标库的样式

// 重复导入 ElementPlus 的样式文件，可删除其中一处以避免重复加载
import 'element-plus/dist/index.css';
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
const vuetify = createVuetify()


// 创建一个新的 Vue 应用实例，传入根组件 App
const app = createApp(App);
// 创建一个新的 Pinia 实例，用于状态管理
const pinia = createPinia();
// 创建一个持久化状态插件实例
const persist = createPersistedState();
// 将持久化状态插件应用到 Pinia 实例上，这样 Pinia 中的状态就会被持久化存储
pinia.use(persist)
// 将 Pinia 实例应用到 Vue 应用中，使得组件可以使用 Pinia 进行状态管理
app.use(pinia)
// 将路由配置应用到 Vue 应用中，使得应用可以根据 URL 进行页面导航
app.use(router)
// 将自定义的状态管理实例应用到 Vue 应用中
app.use(store)
// 将 ElementPlus 组件库应用到 Vue 应用中，并设置语言包为中文
app.use(ElementPlus, { locale });
// 将 Vue 应用挂载到 id 为 app 的 DOM 元素上，使得应用在页面上显示
app.use(vuetify)
app.mount('#app')
