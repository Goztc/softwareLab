<template>
  <div v-if="sources && sources.length > 0" class="document-references">
    <div class="references-header">
      <el-icon class="references-icon"><Document /></el-icon>
      <span class="references-title">参考文档</span>
    </div>
    
    <div class="references-list">
      <div 
        v-for="(source, index) in sources" 
        :key="index"
        class="reference-item"
        @click="showSourceDetail(source)"
      >
        <div class="reference-header">
          <el-icon class="file-icon"><Files /></el-icon>
          <span class="file-path">{{ source.source }}</span>
        </div>
        <div class="reference-content">
          {{ truncateContent(source.content) }}
        </div>
      </div>
    </div>

    <!-- 文档内容详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedSource?.source || '文档内容'"
      width="60%"
      class="source-dialog"
    >
      <div class="source-content">
        <pre>{{ selectedSource?.content }}</pre>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Document, Files } from '@element-plus/icons-vue'

interface DocumentSource {
  source: string
  content: string
  absolute_path?: string
}

interface Props {
  sources: DocumentSource[]
}

defineProps<Props>()

const dialogVisible = ref(false)
const selectedSource = ref<DocumentSource | null>(null)

const truncateContent = (content: string, maxLength: number = 100): string => {
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

const showSourceDetail = (source: DocumentSource) => {
  selectedSource.value = source
  dialogVisible.value = true
}
</script>

<style lang="scss" scoped>
.document-references {
  margin-top: 12px;
  padding: 12px;
  background-color: #f8fbff;
  border: 1px solid #e1ecff;
  border-radius: 8px;
  font-size: 13px;

  .references-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    color: #1a73e8;
    font-weight: 500;

    .references-icon {
      margin-right: 6px;
      font-size: 14px;
    }

    .references-title {
      font-size: 14px;
    }
  }

  .references-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .reference-item {
    padding: 8px;
    background-color: white;
    border: 1px solid #e6f2ff;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: #b3d9ff;
      background-color: #f0f8ff;
    }

    .reference-header {
      display: flex;
      align-items: center;
      margin-bottom: 4px;
      color: #1a73e8;
      font-weight: 500;

      .file-icon {
        margin-right: 6px;
        font-size: 12px;
      }

      .file-path {
        font-size: 12px;
        word-break: break-all;
      }
    }

    .reference-content {
      color: #666;
      line-height: 1.4;
      font-size: 12px;
    }
  }
}

.source-dialog {
  .source-content {
    max-height: 400px;
    overflow-y: auto;
    
    pre {
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      line-height: 1.5;
      color: #333;
      background-color: #f8f9fa;
      padding: 16px;
      border-radius: 6px;
      border: 1px solid #e9ecef;
    }
  }
}
</style> 