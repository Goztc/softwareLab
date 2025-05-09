// 从 'node:url' 模块导入 fileURLToPath 和 URL 函数，用于将文件 URL 转换为文件路径
import { fileURLToPath, URL } from 'node:url'

// 从 'vite' 模块导入 defineConfig 函数，用于定义 Vite 配置
import { defineConfig } from 'vite'
// 从 '@vitejs/plugin-vue' 模块导入 vue 插件，用于支持 Vue 单文件组件
import vue from '@vitejs/plugin-vue'
// 从 'node:path' 模块导入 path 模块，用于处理和转换文件路径'
// 自定义请求日志记录器
// 自定义请求日志记录器
function createRequestLogger() {
  return {
    name: 'request-logger',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        // 跳过静态资源请求
        if (isStaticAsset(req.originalUrl)) {
          return next()
        }
        const startTime = Date.now()
        const { method, originalUrl, headers } = req

        // 收集请求参数（GET/POST）
        let requestData = ''
        req.on('data', chunk => {
          requestData += chunk
        })

        req.on('end', () => {
          const responseTime = Date.now() - startTime

          // 完整日志对象
          const requestLog = {
            timestamp: new Date().toISOString(),
            method,
            url: originalUrl,
            transformedUrl: req.url, // 代理后的URL
            headers: {
              // ...headers,
              // 过滤敏感头信息
              authorization: headers.authorization ? headers.authorization : undefined,
              cookie: headers.cookie ? headers.cookie : undefined
            },
            params: method === 'GET' ? getQueryParams(originalUrl) : null,
            body: method !== 'GET' && requestData ? tryParseJson(requestData) : null,
            proxyTarget: 'http://localhost:8080',
            responseTime: `${responseTime}ms`
          }

          // 输出彩色日志到控制台
          console.log('\n' + '='.repeat(50))
          console.log('\x1b[36m%s\x1b[0m', '⬆️ 代理请求信息:')
          console.dir(requestLog, { depth: null, colors: true })
          console.log('='.repeat(50) + '\n')
        })

        next()
      })
    }
  }
}

// 判断是否为静态资源路径
function isStaticAsset(url: string): boolean {
  const staticPatterns = [
    /^\/src\/assets\//i,
    /^\/src\/components\//i,
    /^\/node_modules\//i,
    /\.(png|jpg|jpeg|gif|svg|ico|css|js|woff|woff2|ttf|eot)$/i
  ]
  return staticPatterns.some(pattern => pattern.test(url))
}

// 辅助函数：解析URL查询参数
function getQueryParams(url) {
  const query = url.split('?')[1]
  if (!query) return {}

  return Object.fromEntries(new URLSearchParams(query))
}

// 辅助函数：尝试解析JSON
function tryParseJson(data) {
  try {
    return JSON.parse(data)
  } catch {
    return data
  }
}

// 参考 Vite 官方文档进行配置：https://vitejs.dev/config/
// 使用 defineConfig 函数定义 Vite 配置并导出
export default defineConfig({
  // plugins 选项用于配置 Vite 插件，这里使用 vue 插件来支持 Vue 项目
  // 配置方式：在数组中添加需要的插件实例
  // 作用：让 Vite 能够正确处理 Vue 单文件组件（.vue 文件）
  plugins: [
    vue(),
    createRequestLogger()
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
  esbuild: {
    jsxFactory: 'h',
    jsxFragment: 'Fragment'
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
        },
        // 添加代理事件监听
        //   configure: (proxy, options) => {
        //     proxy.on('proxyReq', (proxyReq, req, res) => {
        //       console.log('\x1b[32m➡️ 转发到:\x1b[0m', `${options.target}${req.url}`);
        //     });

        //     proxy.on('proxyRes', (proxyRes, req, res) => {
        //       const chunks = [];
        //       proxyRes.on('data', chunk => chunks.push(chunk));

        //       proxyRes.on('end', () => {
        //         const body = Buffer.concat(chunks).toString('utf8');

        //         // 仅打印响应体（自动格式化JSON）
        //         try {
        //           const parsedBody = JSON.parse(body);
        //           console.log('\x1b[36m⬅️ 响应体:\x1b[0m', JSON.stringify(parsedBody, null, 2));
        //         } catch {
        //           console.log('\x1b[36m⬅️ 响应体:\x1b[0m', body);
        //         }
        //       });
        //     });

        //     proxy.on('error', err => {
        //       console.error('\x1b[31m❌ 代理错误:\x1b[0m', err.message);
        //     });
        //   }
      }
    }
  }
})


function logTransformedPath(path) {
  // 这里实现你的逻辑，比如发送到服务器，或者存储到localStorage等  
  console.log('Transformed path:', path);
}


