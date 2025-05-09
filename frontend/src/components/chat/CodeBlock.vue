<template>
  <div class="code-block">
    <div class="code-header">
      <span v-if="language" class="language">{{ language }}</span>
      <el-button
        class="copy-btn"
        type="text"
        @click="copyCode"
        :class="{ copied }"
      >
        <el-icon><DocumentCopy /></el-icon>
        {{ copied ? '已复制' : '复制代码' }}
      </el-button>
    </div>
    <pre><code :class="language">{{ code }}</code></pre>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import hljs from 'highlight.js'

const props = defineProps<{
  code: string
  language?: string
}>()

const copied = ref(false)

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(props.code)
    copied.value = true
    ElMessage.success('代码已复制到剪贴板')
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

// 代码高亮
onMounted(() => {
  hljs.highlightAll()
})
</script>

<style lang="scss" scoped>
.code-block {
  margin: 16px 0;
  background: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;

  .code-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px;
    background: #eaeaea;
    border-bottom: 1px solid #ddd;

    .language {
      color: #666;
      font-size: 12px;
      text-transform: uppercase;
    }

    .copy-btn {
      color: #666;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;

      &:hover {
        color: #333;
      }

      &.copied {
        color: #67c23a;
      }
    }
  }

  pre {
    margin: 0;
    padding: 16px;
    overflow-x: auto; /* 水平滚动 */
    white-space: pre-wrap; /* 允许自动换行 */
    word-break: break-all;

    code {
      font-family: 'Fira Code', monospace;
      font-size: 14px;
      line-height: 1.5;
      color: #333;
      background-color: transparent;
    }
  }
}
</style>