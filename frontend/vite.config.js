// 从 'node:url' 模块导入 fileURLToPath 和 URL 函数，用于将文件 URL 转换为文件路径
import { fileURLToPath, URL } from 'node:url'

// 从 'vite' 模块导入 defineConfig 函数，用于定义 Vite 配置
import { defineConfig } from 'vite'
// 从 '@vitejs/plugin-vue' 模块导入 vue 插件，用于支持 Vue 单文件组件
import vue from '@vitejs/plugin-vue'
// 从 'node:path' 模块导入 path 模块，用于处理和转换文件路径
import path from 'node:path'

// 参考 Vite 官方文档进行配置：https://vitejs.dev/config/
// 使用 defineConfig 函数定义 Vite 配置并导出
export default defineConfig({
  // plugins 选项用于配置 Vite 插件，这里使用 vue 插件来支持 Vue 项目
  // 配置方式：在数组中添加需要的插件实例
  // 作用：让 Vite 能够正确处理 Vue 单文件组件（.vue 文件）
  plugins: [
    vue(),
  ],
  // resolve 选项用于配置模块解析规则
  resolve: {
    // alias 选项用于配置路径别名，方便在项目中引用模块
    // 配置方式：对象形式，键为别名，值为实际路径
    // 作用：可以使用别名 '@' 来代替 './src' 路径，简化模块引用
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // server 选项用于配置开发服务器
  server: {
    // proxy 选项用于配置开发服务器的代理规则，解决跨域问题
    proxy: {
      // 匹配所有以 '/api' 开头的请求路径
      '/api': {
        // target 选项指定代理请求的目标地址，即后台服务所在的源
        // 配置方式：填写后台服务的完整 URL
        // 作用：将以 '/api' 开头的请求转发到指定的后台服务
        target: 'http://localhost:8080',
        // 注释掉的目标地址，可根据需要切换
        // target: 'http://159.75.116.168:8080',
        // changeOrigin 选项设置为 true 表示修改请求的源，让服务器以为请求来自目标地址
        // 配置方式：布尔值，true 或 false
        // 作用：解决跨域请求时的同源策略限制
        changeOrigin: true,
        // rewrite 选项用于重写请求路径
        // 配置方式：传入一个函数，函数接收请求路径作为参数，返回重写后的路径
        // 作用：将请求路径中的 '/api' 前缀去掉，并记录转换后的路径
        rewrite: (path) => {
          // 将请求路径中的 '/api' 前缀替换为空字符串
          const transformedPath = path.replace(/^\/api/, '');
          // 调用 logTransformedPath 函数记录转换后的路径
          logTransformedPath(transformedPath);
          return transformedPath;
        }
      }
    }
  }
})



function logTransformedPath(path) {
  // 这里实现你的逻辑，比如发送到服务器，或者存储到localStorage等  
  console.log('Transformed path:', path);
}


