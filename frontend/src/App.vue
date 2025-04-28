<script setup>

</script>

<template>
  <!-- 
  <router-view> 是 Vue Router 提供的一个内置组件，用于在页面中显示当前路由对应的组件。
  当用户在应用中导航到不同的路由时，<router-view> 会根据当前的路由路径动态渲染对应的组件。

  在 App.vue 中添加 <router-view> 是因为 App.vue 通常是 Vue 应用的根组件，
  它会在整个应用的生命周期内保持存在。通过在 App.vue 中放置 <router-view>，
  可以让不同的路由组件在这个固定的位置进行切换和显示，实现单页面应用（SPA）的路由功能。

  与 <router-view> 相关的其他配置主要涉及 Vue Router 的设置：
  1. 路由配置：需要在路由文件（通常是 router/index.js 或 router/index.ts）中定义路由规则，
     每个路由规则会指定一个路径和对应的组件。例如：
     import { createRouter, createWebHistory } from 'vue-router';
     import Home from '@/views/Home.vue';
     import About from '@/views/About.vue';

     const routes = [
       { path: '/', component: Home },
       { path: '/about', component: About }
     ];

     const router = createRouter({
       history: createWebHistory(),
       routes
     });

     export default router;
  2. 路由实例：需要在 main.js 或 main.ts 中引入并使用路由实例。例如：
     import { createApp } from 'vue';
     import App from './App.vue';
     import router from './router';

     const app = createApp(App);
     app.use(router);
     app.mount('#app');

  这些配置之间的关联是：路由配置定义了不同路径对应的组件，路由实例将这些配置应用到 Vue 应用中，
  当用户在应用中导航到不同的路径时，Vue Router 会根据路由配置找到对应的组件，
  并将其渲染到 <router-view> 所在的位置。
  -->
  <router-view></router-view>
</template>

<style scoped></style>
