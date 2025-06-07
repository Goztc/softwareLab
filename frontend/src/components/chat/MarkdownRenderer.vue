<template>
  <div class="markdown-renderer">
    <vue3-markdown-it 
      :source="content" 
      :config="markdownConfig"
      class="markdown-content"
    />
  </div>
</template>

<script setup lang="ts">
import Vue3MarkdownIt from 'vue3-markdown-it'

const props = defineProps<{
  content: string
}>()

// Markdown配置
const markdownConfig = {
  html: false,        // 禁用HTML标签以确保安全
  xhtmlOut: false,
  breaks: true,       // 支持换行
  linkify: true,      // 自动链接识别
  typographer: true,  // 启用排版优化
  highlight: function (str: string, lang: string) {
    // 可以集成代码高亮，如果需要的话
    return '<pre class="hljs"><code>' + str + '</code></pre>'
  }
}
</script>

<style lang="scss" scoped>
.markdown-renderer {
  width: 100%;
  
  .markdown-content {
    line-height: 1.6;
    color: #333;
    font-size: 14px;
    
    // 标题样式
    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
      margin: 16px 0 8px 0;
      font-weight: 600;
      line-height: 1.4;
    }
    
    :deep(h1) { font-size: 20px; color: #2c3e50; }
    :deep(h2) { font-size: 18px; color: #34495e; }
    :deep(h3) { font-size: 16px; color: #34495e; }
    
    // 段落样式
    :deep(p) {
      margin: 8px 0;
      text-align: justify;
      word-wrap: break-word;
    }
    
    // 列表样式
    :deep(ul), :deep(ol) {
      margin: 8px 0;
      padding-left: 20px;
      
      li {
        margin: 4px 0;
        line-height: 1.5;
      }
    }
    
    :deep(ul) {
      list-style-type: disc;
      
      ul {
        list-style-type: circle;
        
        ul {
          list-style-type: square;
        }
      }
    }
    
    :deep(ol) {
      list-style-type: decimal;
    }
    
    // 代码样式
    :deep(code) {
      background-color: #f5f5f5;
      border: 1px solid #e0e0e0;
      border-radius: 3px;
      padding: 2px 4px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      color: #d63384;
    }
    
    :deep(pre) {
      background-color: #f8f9fa;
      border: 1px solid #e9ecef;
      border-radius: 6px;
      padding: 12px;
      margin: 12px 0;
      overflow-x: auto;
      
      code {
        background: none;
        border: none;
        padding: 0;
        color: #333;
      }
    }
    
    // 引用样式
    :deep(blockquote) {
      border-left: 4px solid #dfe2e5;
      padding-left: 16px;
      margin: 12px 0;
      color: #6a737d;
      background-color: #f6f8fa;
      padding: 8px 16px;
      border-radius: 0 6px 6px 0;
    }
    
    // 表格样式
    :deep(table) {
      border-collapse: collapse;
      width: 100%;
      margin: 12px 0;
      
      th, td {
        border: 1px solid #dfe2e5;
        padding: 8px 12px;
        text-align: left;
      }
      
      th {
        background-color: #f6f8fa;
        font-weight: 600;
      }
      
      tr:nth-child(even) {
        background-color: #f6f8fa;
      }
    }
    
    // 分割线样式
    :deep(hr) {
      border: none;
      border-top: 1px solid #e1e4e8;
      margin: 16px 0;
    }
    
    // 链接样式
    :deep(a) {
      color: #0366d6;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    // 强调样式
    :deep(strong) {
      font-weight: 600;
    }
    
    :deep(em) {
      font-style: italic;
    }
  }
}
</style> 