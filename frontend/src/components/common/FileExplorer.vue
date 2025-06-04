<template>
  <div class="file-explorer">
    <!-- 侧边栏文件夹树 -->
    <el-aside width="280px" class="sidebar">
      <div class="tree-header">
        <h3>文件夹结构</h3>
        <el-button type="primary" size="small" @click="createNewFolder" circle>
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      <div class="tree-container">
        <el-tree
          v-if="fileStore.folderTree.length"
          :data="fileStore.folderTree"
          :props="treeProps"
          :expand-on-click-node="false"
          :highlight-current="true"
          node-key="id"
          :current-node-key="fileStore.currentFolderId"
          @node-click="handleNodeClick"
          class="folder-tree"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <el-icon><FolderIcom /></el-icon>
              <span>{{ data.folderName }}</span>
            </span>
          </template>
        </el-tree>
        <el-empty v-else description="暂无文件夹" :image-size="80">
          <el-icon><FolderIcom /></el-icon>
        </el-empty>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <!-- 路径导航 -->
      <div class="navigation-bar">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item 
            v-for="(item, index) in fileStore.currentFolderPath" 
            :key="item.id"
          >
            <a @click="goBack(fileStore.currentFolderPath.length - 1 - index)">
              <el-icon v-if="index === 0"><HomeFilled /></el-icon>
              {{ item.folderName }}
            </a>
          </el-breadcrumb-item>
        </el-breadcrumb>
        
        <div class="action-bar">
          <el-button 
            plain 
            @click="goBack(1)" 
            :disabled="fileStore.currentFolderPath.length <= 1"
            size="small"
          >
            <el-icon><ArrowUp /></el-icon>返回上级
          </el-button>
          <el-button type="primary" @click="showUploadDialog = true" size="small">
            <el-icon><Upload /></el-icon>上传
          </el-button>
          <el-button type="success" @click="createNewFolder" size="small">
            <el-icon><FolderAdd /></el-icon>新建文件夹
          </el-button>
          <el-button type="info" @click="showCreateTextDialog = true" size="small">
            <el-icon><Document /></el-icon>新建文本
          </el-button>
        </div>
      </div>

      <!-- 内容列表 -->
      <div class="content-container">
        <el-skeleton v-if="fileStore.loading" :rows="6" animated />
        <template v-else>
          <!-- 文件夹列表 -->
          <div v-if="fileStore.currentFolderContents.folders.length" class="folder-list">
            <h4 class="section-title">
              <el-icon><FolderIcom /></el-icon>
              <span>文件夹</span>
            </h4>
            <el-scrollbar>
              <div class="grid-container">
                <div 
                  v-for="folder in fileStore.currentFolderContents.folders"
                  :key="folder.id"
                  class="grid-item"
                >
                  <el-card 
                    shadow="hover" 
                    class="folder-item" 
                    @click="loadFolder(folder.id)"
                    :body-style="{ padding: '16px' }"
                  >
                    <div class="card-icon">
                      <el-icon color="#409EFF" :size="36"><FolderIcom /></el-icon>
                    </div>
                    <div class="card-content">
                      <div class="name">{{ folder.folderName }}</div>
                      <div class="meta">--</div>
                    </div>
                    <div class="card-actions">
                      <el-tooltip content="重命名" placement="top">
                        <el-button 
                          size="small" 
                          @click.stop="renameItem(folder)"
                          circle
                        >
                          <el-icon><Edit /></el-icon>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="删除" placement="top">
                        <el-button 
                          size="small" 
                          @click.stop="deleteFolder(folder.id)"
                          circle
                          type="danger"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </el-tooltip>
                    </div>
                  </el-card>
                </div>
              </div>
            </el-scrollbar>
          </div>

          <!-- 文件列表 -->
          <div v-if="fileStore.currentFolderContents.files.length" class="file-list">
            <h4 class="section-title">
              <el-icon><Files /></el-icon>
              <span>文件</span>
            </h4>
            <el-scrollbar>
              <div class="grid-container">
                <div 
                  v-for="file in fileStore.currentFolderContents.files"
                  :key="file.id"
                  class="grid-item"
                >
                  <el-card shadow="hover" class="file-item" :body-style="{ padding: '16px' }">
                    <div class="card-icon">
                      <el-icon :color="getFileColor(file.fileName)" :size="36">
                        <component :is="getFileIcon(file.fileName)" />
                      </el-icon>
                    </div>
                    <div class="card-content">
                      <div class="name">{{ file.fileName }}</div>
                    </div>
                    <div class="card-actions">
                      <el-tooltip content="下载" placement="top">
                        <el-button 
                          size="small" 
                          @click.stop="downloadFile(file)"
                          circle
                        >
                          <el-icon><Download /></el-icon>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="编辑" placement="top">
                        <el-button 
                          size="small" 
                          @click.stop="editFile(file)"
                          circle
                          :disabled="!isTextFile(file.fileName)"
                        >
                          <el-icon><Edit /></el-icon>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="删除" placement="top">
                        <el-button 
                          size="small" 
                          @click.stop="deleteFile(file.id)"
                          circle
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </el-tooltip>
                    </div>
                  </el-card>
                </div>
              </div>
            </el-scrollbar>
          </div>

          <el-empty 
            v-if="!fileStore.currentFolderContents.folders.length && !fileStore.currentFolderContents.files.length" 
            description="当前文件夹为空" 
            :image-size="100"
          >
            <el-button type="primary" @click="createNewFolder">新建文件夹</el-button>
            <el-button type="info" @click="showCreateTextDialog = true">新建文本</el-button>
          </el-empty>
        </template>
      </div>

      <!-- 上传文件对话框 -->
      <el-dialog 
        v-model="showUploadDialog" 
        title="上传文件" 
        width="500px"
        custom-class="upload-dialog"
      >
        <el-upload
          drag
          :auto-upload="false"
          :show-file-list="true"
          :on-change="handleFileUpload"
          multiple
        >
          <el-icon :size="50" color="var(--el-color-primary)"><Upload /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持上传任意类型文件，单文件不超过 50MB
            </div>
          </template>
        </el-upload>
        <template #footer>
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="submitUpload">开始上传</el-button>
        </template>
      </el-dialog>

      <!-- 新建文件夹对话框 -->
      <el-dialog 
        v-model="showNewFolderDialog" 
        title="新建文件夹" 
        width="400px"
      >
        <el-form :model="newFolderForm" label-width="80px">
          <el-form-item label="文件夹名">
            <el-input v-model="newFolderForm.name" placeholder="请输入文件夹名称" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showNewFolderDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmCreateFolder">确定</el-button>
        </template>
      </el-dialog>

      <!-- 新建文本文件对话框 -->
      <el-dialog 
        v-model="showCreateTextDialog" 
        title="新建文本文件" 
        width="600px"
      >
        <el-form :model="createTextForm" label-width="100px">
          <el-form-item label="文件名">
            <el-input v-model="createTextForm.fileName" placeholder="请输入文件名（例如：note.txt）" />
          </el-form-item>
          <el-form-item label="文件内容">
            <el-input
              type="textarea"
              v-model="createTextForm.content"
              placeholder="请输入文件内容"
              :rows="8"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateTextDialog = false">取消</el-button>
          <el-button type="primary" @click="createTextFile">确定</el-button>
        </template>
      </el-dialog>

      <!-- 编辑文件对话框 -->
      <el-dialog 
        v-model="showEditFileDialog" 
        title="编辑文本文件" 
        width="600px"
      >
        <el-form :model="editFileForm" label-width="100px">
          <el-form-item label="文件名">
            <el-input v-model="editFileForm.fileName" disabled />
          </el-form-item>
          <el-form-item label="文件内容">
            <el-input
              type="textarea"
              v-model="editFileForm.content"
              placeholder="请输入文件内容"
              :rows="8"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditFileDialog = false">取消</el-button>
          <el-button type="primary" @click="updateFile">确定</el-button>
        </template>
      </el-dialog>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useFileStore } from '@/stores/file'
import { ElMessage, ElMessageBox, UploadFile } from 'element-plus'
import type { MyFile, Folder } from '@/types'
import {
  Folder as FolderIcom,
  Plus,
  Upload,
  ArrowUp,
  Edit,
  Delete,
  Document,
  Picture,
  Files,
  VideoCamera,
  Headset,
  HomeFilled,
  FolderAdd,
  Download
} from '@element-plus/icons-vue'

import { fileApi } from '@/api/file';
const fileStore = useFileStore();
const showUploadDialog = ref(false);
const showNewFolderDialog = ref(false);
const showCreateTextDialog = ref(false);
const showEditFileDialog = ref(false);
const newFolderForm = ref({
  name: ''
});
const createTextForm = ref({
  fileName: '',
  content: ''
});
const editFileForm = ref({
  fileId: 0,
  fileName: '',
  content: ''
});

const treeProps = {
  children: 'children',
  label: 'folderName'
};


// 初始化加载数据
onMounted(() => {
  fileStore.initStore()
})

// 加载指定文件夹
const loadFolder = (folderId: number) => {
  fileStore.loadFolderContents(folderId)
}

// 处理树节点点击
const handleNodeClick = (data: Folder) => {
  loadFolder(data.id)
}

// 返回上级
const goBack = (level: number = 1) => {
  if (fileStore.currentFolderPath.length <= level) return
  
  const targetIndex = fileStore.currentFolderPath.length - 1 - level
  const targetFolder = fileStore.currentFolderPath[targetIndex]
  
  if (targetFolder) {
    fileStore.loadFolderContents(targetFolder.id)
  }
}

// 处理文件上传
const handleFileUpload = async (uploadFile: UploadFile) => {
  try {
    if (!uploadFile.raw) {
      throw new Error("未获取到文件对象")
    }
    await fileStore.uploadFile(uploadFile.raw)
    showUploadDialog.value = false
    ElMessage.success('文件上传成功')
  } catch (error) {
    ElMessage.error(`文件上传失败: ${error instanceof Error ? error.message : String(error)}`)
  }
}

// 提交上传
const submitUpload = () => {
  // 这里可以处理批量上传逻辑
  ElMessage.success('开始上传文件')
  showUploadDialog.value = false
}

// 创建新文件夹
const createNewFolder = () => {
  newFolderForm.value.name = ''
  showNewFolderDialog.value = true
}

const confirmCreateFolder = async () => {
  if (!newFolderForm.value.name.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  
  try {
    await fileStore.createFolder(newFolderForm.value.name)
    showNewFolderDialog.value = false
    ElMessage.success('文件夹创建成功')
  } catch (error) {
    ElMessage.error('文件夹创建失败')
  }
}

// 创建文本文件
const createTextFile = async () => {
  if (!createTextForm.value.fileName.trim()) {
    ElMessage.warning('请输入文件名');
    return;
  }
  if (!createTextForm.value.content.trim()) {
    ElMessage.warning('请输入文件内容');
    return;
  }

  try {
    await fileStore.uploadFileByText(createTextForm.value.content, createTextForm.value.fileName);
    showCreateTextDialog.value = false;
    ElMessage.success('文本文件创建成功');
    createTextForm.value = { fileName: '', content: '' }; // 重置表单
  } catch (error) {
    ElMessage.error(`文本文件创建失败: ${error instanceof Error ? error.message : String(error)}`);
  }
};

// 编辑文件
const editFile = async (file: MyFile) => {
  if (!isTextFile(file.fileName)) {
    ElMessage.warning('仅支持编辑 .txt 文件');
    return;
  }

  try {
    const { code, data, message } = await fileApi.getContent(file.id);
    if (code !== 0) throw new Error(message);

    editFileForm.value = {
      fileId: file.id,
      fileName: file.fileName,
      content: data
    };
    showEditFileDialog.value = true;
  } catch (error) {
    ElMessage.error(`获取文件内容失败: ${error instanceof Error ? error.message : String(error)}`);
  }
};

// 更新文件（删除并重新创建）
const updateFile = async () => {
  if (!editFileForm.value.content.trim()) {
    ElMessage.warning('请输入文件内容');
    return;
  }

  try {
    await fileStore.deleteFile(editFileForm.value.fileId);
    await fileStore.uploadFileByText(editFileForm.value.content, editFileForm.value.fileName);
    showEditFileDialog.value = false;
    ElMessage.success('文件更新成功');
    editFileForm.value = { fileId: 0, fileName: '', content: '' }; // 重置表单
  } catch (error) {
    ElMessage.error(`文件更新失败: ${error instanceof Error ? error.message : String(error)}`);
  }
};

// 判断是否为文本文件
const isTextFile = (fileName: string) => {
  return fileName.toLowerCase().endsWith('.txt');
};


// 删除文件
const deleteFile = async (fileId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个文件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await fileStore.deleteFile(fileId)
    ElMessage.success('文件删除成功')
  } catch (error) {
    // 用户取消删除
  }
}

// 删除文件夹
const deleteFolder = async (folderId: number) => {
  try {
    // 检查是否为根文件夹或当前文件夹
    const isRootFolder = fileStore.folderTree.some(folder => folder.id === folderId)
    const isCurrentFolder = fileStore.currentFolderId === folderId
    
    if (isCurrentFolder) {
      ElMessage.warning('不能删除当前所在的文件夹')
      return
    }
    
    const confirmText = isRootFolder 
      ? '确定要删除这个根文件夹吗？这将删除其中的所有内容！'
      : '确定要删除这个文件夹吗？删除后文件夹内的所有内容都将丢失！'
    
    await ElMessageBox.confirm(
      confirmText,
      '警告', 
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: true
      }
    )
    
    await fileStore.deleteFolder(folderId)
    ElMessage.success('文件夹删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('文件夹删除失败')
    }
  }
}

// 下载文件
const downloadFile = (file: MyFile) => {
  ElMessage.success(`开始下载: ${file.fileName}`)
  // 这里添加实际下载逻辑
}

// 重命名项目
const renameItem = (item: MyFile | Folder) => {
  ElMessageBox.prompt('请输入新名称', '重命名', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: 'fileName' in item ? item.fileName : item.folderName
  }).then(async ({ value }) => {
    if (value) {
      try {
        await fileStore.renameItem({
          id: item.id,
          newName: value,
          isFolder: 'folderName' in item
        })
        ElMessage.success('重命名成功')
      } catch (error) {
        ElMessage.error('重命名失败')
      }
    }
  }).catch(() => {
    // 用户取消
  })
}

// 获取文件图标
const getFileIcon = (fileName: string) => {
  const extension = fileName.split('.').pop()?.toLowerCase()
  switch(extension) {
    case 'pdf': return Document
    case 'doc':
    case 'docx': return Document
    case 'xls':
    case 'xlsx': return Document
    case 'ppt':
    case 'pptx': return Document
    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'gif': return Picture
    case 'mp4':
    case 'avi':
    case 'mov': return VideoCamera
    case 'mp3':
    case 'wav': return Headset
    default: return Files
  }
}

// 获取文件图标颜色
const getFileColor = (fileName: string) => {
  const extension = fileName.split('.').pop()?.toLowerCase()
  switch(extension) {
    case 'pdf': return '#FF5252'
    case 'doc':
    case 'docx': return '#2E7D32'
    case 'xls':
    case 'xlsx': return '#388E3C'
    case 'ppt':
    case 'pptx': return '#D32F2F'
    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'gif': return '#FF6D00'
    case 'mp4':
    case 'avi':
    case 'mov': return '#6A1B9A'
    case 'mp3':
    case 'wav': return '#0288D1'
    default: return '#607D8B'
  }
}

// 格式化文件大小
const formatFileSize = (bytes?: number) => {
  if (!bytes) return '--'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`
}
</script>

<style lang="scss" scoped>
.file-explorer {
  display: flex;
  height: 100vh;
  background-color: var(--el-bg-color-page);
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.sidebar {
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  .tree-header {
    padding: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid var(--el-border-color-light);

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .el-button {
      transition: all 0.2s ease;
    }
  }

  .tree-container {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
  }
}

.folder-tree {
  background-color: transparent;
  
  :deep(.el-tree-node__content) {
    height: 36px;
    border-radius: 4px;
    transition: all 0.2s ease;
    
    &:hover {
      background-color: var(--el-color-primary-light-9);
    }
  }
  
  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background-color: var(--el-color-primary-light-8);
    font-weight: 500;
  }
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  padding-right: 8px;
  
  .el-icon {
    margin-right: 8px;
    color: var(--el-color-warning);
    font-size: 16px;
  }
}

.main-content {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--el-bg-color-page);
  padding: 0;
}

.navigation-bar {
  padding: 12px 20px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  
  .breadcrumb {
    flex: 1;
    min-width: 200px;
    
    .el-breadcrumb__item {
      font-size: 14px;
      
      .el-icon {
        margin-right: 4px;
      }
      
      a {
        display: flex;
        align-items: center;
        color: var(--el-text-color-regular);
        transition: color 0.2s ease;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }
  
  .action-bar {
    display: flex;
    gap: 8px;
    
    .el-button {
      transition: all 0.2s ease;
    }
  }
}

.content-container {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 15px;
  color: var(--el-text-color-secondary);
  font-weight: 600;
  display: flex;
  align-items: center;
  
  .el-icon {
    margin-right: 8px;
    font-size: 18px;
  }
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.folder-item, .file-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: var(--el-color-primary-light-5);
  }
}

.card-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.card-content {
  text-align: center;
  
  .name {
    font-weight: 500;
    margin-bottom: 4px;
    word-break: break-word;
    font-size: 14px;
    color: var(--el-text-color-primary);
  }
  
  .meta {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}

.card-actions {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  gap: 6px;
  
  .el-button {
    padding: 6px;
    
    &.is-circle.el-button--danger {
      background-color: #fef0f0;
      border-color: #fbc4c4;
      color: #f56c6c;
      
      &:hover {
        background-color: #f56c6c;
        border-color: #f56c6c;
        color: white;
      }
    }
  }
}

:deep(.upload-dialog) {
  .el-upload-dragger {
    padding: 30px;
    border-radius: 8px;
    border: 2px dashed var(--el-border-color);
    transition: border-color 0.3s;
    
    &:hover {
      border-color: var(--el-color-primary);
    }
    
    .el-upload__text {
      margin-top: 16px;
      font-size: 14px;
      color: var(--el-text-color-regular);
      
      em {
        color: var(--el-color-primary);
        font-style: normal;
      }
    }
    
    .el-upload__tip {
      margin-top: 12px;
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }
}

@media (max-width: 768px) {
  .file-explorer {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100% !important;
    height: 40vh;
    border-right: none;
    border-bottom: 1px solid var(--el-border-color-light);
  }
  
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
  
  .navigation-bar {
    flex-direction: column;
    align-items: flex-start;
    
    .action-bar {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>