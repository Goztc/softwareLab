<template>
  <div class="chat-app-container">
    <!-- 可伸缩侧边栏 -->
    <div class="sidebar" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
      <div class="sidebar-header">
        <h3 v-if="!isSidebarCollapsed">历史对话</h3>
        <el-button 
          @click="toggleSidebar" 
          :icon="isSidebarCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'" 
          circle 
          class="collapse-btn"
        />
      </div>

      <div class="session-list">
        <el-scrollbar>
          <div 
            v-for="session in chatStore.sessions" 
            :key="session.id"
            class="session-item"
            :class="{ 'active': session.id === chatStore.currentSessionId }"
            @click="switchSession(session.id)"
            @dblclick="startRenameSession(session.id)"
          >
            <el-icon class="session-icon"><ChatDotRound /></el-icon>
            
            <!-- 正常显示状态 -->
            <span 
              v-if="!isSidebarCollapsed && editingSessionId !== session.id" 
              class="session-name"
            >
              {{ session.sessionName }}
            </span>
            
            <!-- 编辑状态 -->
            <el-input
              v-if="!isSidebarCollapsed && editingSessionId === session.id"
              v-model="editingSessionName"
              class="session-name-input"
              size="small"
              @blur="finishRenameSession"
              @keyup.enter="finishRenameSession"
              @keyup.esc="cancelRenameSession"
              ref="sessionNameInput"
            />
            
            <!-- 折叠状态显示 -->
            <el-tooltip 
              v-if="isSidebarCollapsed" 
              :content="session.sessionName" 
              placement="right"
            >
              <span class="session-name-collapsed">{{ session.sessionName.charAt(0) }}</span>
            </el-tooltip>
          </div>
        </el-scrollbar>
      </div>

      <div class="sidebar-footer">
        <el-button 
          v-if="!isSidebarCollapsed"
          type="primary" 
          @click="createNewSession"
          class="new-chat-btn"
        >
          <el-icon><Plus /></el-icon> 新对话
        </el-button>
        <el-button 
          v-else
          type="primary" 
          @click="createNewSession"
          circle
          class="new-chat-btn-collapsed"
        >
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="main-content">
      <div class="chat-container">
        <!-- 消息区域 -->
        <div class="chat-window" ref="chatWindow">
          <div v-if="chatStore.messages.length === 0" class="empty-message">
            <div class="welcome-message">
              <h2>Qwen RAG</h2>
              <p>开始与AI助手对话，探索无限可能</p>
            </div>
          </div>
    
          <div v-else class="message-list">
            <div 
              v-for="(msg, index) in chatStore.messages" 
              :key="msg.id" 
              class="message-container"
              :class="{ 'user-message-container': msg.role === 'user' }"
            >
              <div class="message-avatar">
                <el-avatar v-if="msg.role === 'user'" :size="32" :src="userAvatar" />
                <el-avatar v-else :size="32" :src="botAvatar" />
              </div>
              <div 
                class="message-content"
                :class="{ 'user-message': msg.role === 'user', 'bot-message': msg.role === 'assistant' }"
              >
                <div class="message-header">
                  <span class="message-role">{{ msg.role === 'user' ? '你' : 'DeepSeek Chat' }}</span>
                  <div class="message-actions">
                    <el-tooltip content="复制" placement="top">
                      <el-button 
                        size="small" 
                        text 
                        @click="copyMessage(msg.content)"
                        class="action-btn"
                      >
                        <el-icon><DocumentCopy /></el-icon>
                      </el-button>
                    </el-tooltip>
                    <el-tooltip 
                      v-if="msg.role === 'user'" 
                      content="修改" 
                      placement="top"
                    >
                      <el-button 
                        size="small" 
                        text 
                        @click="editMessage(index)"
                        class="action-btn"
                      >
                        <el-icon><Edit /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </div>
                <div class="message-text">
                  <template v-for="(block, index) in parseMessage(msg.content)" :key="index">
                    <CodeBlock
                      v-if="block.type === 'code'"
                      :code="block.content"
                      :language="block.language"
                    />
                    <div v-else v-html="block.content"></div>
                  </template>
                </div>
                <div class="message-footer">
                  <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
  
        <!-- 输入区域 -->
        <div class="input-container">
          <!-- 选中文件显示区域 -->
          <div v-if="selectedFiles.length > 0 || selectedFolders.length > 0" class="selected-files-container">
            <div class="selected-files-header">
              <span>已选择的文件和文件夹：</span>
              <el-button size="small" @click="clearSelection" text>清空选择</el-button>
            </div>
            <div class="selected-files-list">
              <el-tag 
                v-for="file in selectedFiles" 
                :key="'file-' + file.id"
                closable
                @close="removeSelectedFile(file.id)"
                class="selected-item"
              >
                <el-icon><Document /></el-icon>
                {{ file.fileName }}
              </el-tag>
              <el-tag 
                v-for="folder in selectedFolders" 
                :key="'folder-' + folder.id"
                closable
                @close="removeSelectedFolder(folder.id)"
                class="selected-item"
                type="warning"
              >
                <el-icon><Folder /></el-icon>
                {{ folder.folderName }}
              </el-tag>
            </div>
          </div>

          <el-input
            v-model="userInput"
            @keyup.enter="sendMessage"
            placeholder="输入你的消息..."
            class="message-input"
            :disabled="isSending"
            :rows="2"
            type="textarea"
            resize="none"
          >
            <template #append>
              <el-button 
                @click="sendMessage" 
                type="primary" 
                :loading="isSending"
                class="send-btn"
              >
                <template #default>
                  <el-icon><Promotion /></el-icon>
                  <span v-if="!isMobile">发送</span>
                </template>
              </el-button>
            </template>
          </el-input>
    
          <div class="action-buttons">
            <el-button 
              @click="deleteCurrentSession" 
              type="danger" 
              plain 
              class="delete-session-btn"
              :disabled="!chatStore.currentSessionId"
            >
              <el-icon><Delete /></el-icon>
              <span v-if="!isMobile">删除会话</span>
            </el-button>
            <el-button 
              @click="clearCurrentSessionHistory" 
              type="info" 
              plain 
              class="reset-btn"
              :disabled="!chatStore.currentSessionId || chatStore.messages.length === 0"
            >
              <el-icon><Delete /></el-icon>
              <span v-if="!isMobile">清空对话</span>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧文件树栏 -->
    <div class="file-sidebar" :class="{ 'file-sidebar-collapsed': isFileSidebarCollapsed }">
      <div class="file-sidebar-header">
        <h3 v-if="!isFileSidebarCollapsed">选择文件</h3>
        <el-button 
          @click="toggleFileSidebar" 
          :icon="isFileSidebarCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'" 
          circle 
          class="collapse-btn"
        />
      </div>

      <div v-if="!isFileSidebarCollapsed" class="file-content">
        <!-- 文件树 -->
        <div class="file-tree-container">
          <el-tree
            ref="fileTreeRef"
            :data="fileTreeData"
            :props="treeProps"
            show-checkbox
            node-key="id"
            :default-expand-all="false"
            :expand-on-click-node="false"
            :check-on-click-node="false"
            lazy
            :load="loadTreeNode"
            @check="handleTreeCheck"
            @node-click="handleTreeNodeClick"
            class="file-tree"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-content">
                  <el-icon class="node-icon" :color="getNodeColor(data)">
                    <component :is="getNodeIcon(data)" />
                  </el-icon>
                  <span class="node-label">{{ data.label }}</span>
                </div>
                <div class="node-meta">
                  <span class="node-type">{{ data.isFolder ? '文件夹' : '文件' }}</span>
                </div>
              </div>
            </template>
          </el-tree>

          <el-empty 
            v-if="!fileTreeData.length" 
            description="暂无文件和文件夹" 
            :image-size="60"
          />
        </div>

        <!-- 选择统计 -->
        <div class="selection-summary">
          <div class="summary-text">
            已选择: {{ getSelectedCount().files }} 个文件, {{ getSelectedCount().folders }} 个文件夹
          </div>
          <el-button size="small" @click="clearTreeSelection" text>清空</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { marked } from 'marked'
import { useChatStore } from '@/stores/chat'
import { ChatDotRound, Plus, Promotion, Delete, DocumentCopy, Edit, Document, 
         Folder, HomeFilled, ArrowUp, Files, Picture, VideoCamera, Headset } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { useDisplay } from 'vuetify'
import { ElMessage } from 'element-plus'
import CodeBlock from "@/components/chat/CodeBlock.vue"
import { chatApi } from '@/api/chat'
import { folderApi } from '@/api/folder'
import type { MyFile, Folder as FolderType } from '@/types'

const chatStore = useChatStore()
const userInput = ref('')
const isSending = ref(false)
const isSidebarCollapsed = ref(false)
const isFileSidebarCollapsed = ref(false)
const chatWindow = ref<HTMLElement | null>(null)
const { mobile } = useDisplay()
const isMobile = computed(() => mobile.value)

// 文件选择相关状态
const currentFolderId = ref(0)
const currentFolderPath = ref<FolderType[]>([{ id: 0, folderName: '根目录', parentId: 0, userId: 0, createTime: '', updateTime: '' }])
const currentFolderContents = ref<{ folders: FolderType[], files: MyFile[] }>({ folders: [], files: [] })
const selectedFileIds = ref(new Set<number>())
const selectedFolderIds = ref(new Set<number>())
const selectedFiles = ref<MyFile[]>([])
const selectedFolders = ref<FolderType[]>([])

// 树形结构相关状态
const fileTreeRef = ref()
const fileTreeData = ref<any[]>([])
const treeProps = {
  children: 'children',
  label: 'label'
}

// 用户和机器人的头像
import userAvatar from '@/assets/bgimg.png';
import botAvatar from '@/assets/ai-avatar.png';

// 消息解析函数
const parseMessage = (content: string) => {
  const blocks = []
  // 匹配格式：```language[:path]\n...code...\n```
  const codeRegex = /```([^`\n]*?)\n([\s\S]+?)(```)/g
  let lastIndex = 0
  let match

  while ((match = codeRegex.exec(content)) !== null) {
    // 提取前面的文本部分
    if (match.index > lastIndex) {
      const textContent = content.slice(lastIndex, match.index)
      blocks.push({
        type: 'text',
        content: marked.parse(textContent)
      })
    }

    // 提取代码块语言和内容
    const language = match[1]?.split(':')[0].trim() || ''
    const codeContent = match[2]

    blocks.push({
      type: 'code',
      language,
      content: codeContent
    })

    lastIndex = match.index + match[0].length
  }

  // 处理最后剩余的文本
  if (lastIndex < content.length) {
    const remainingText = content.slice(lastIndex)
    blocks.push({
      type: 'text',
      content: marked.parse(remainingText)
    })
  }

  return blocks
}

onMounted(async () => {
  await chatStore.loadSessions()
  if (chatStore.sessions.length > 0 && !chatStore.currentSessionId) {
    await chatStore.loadMessageHistory(chatStore.sessions[0].id)
  }
  // 初始化文件树 - 使用与 FileExplorer 相同的逻辑
  await initFileTree()
})

// 初始化文件树的方法
const initFileTree = async () => {
  try {
    // 1. 尝试获取文件夹树
    const treeResponse = await folderApi.getFolderTree()
    
    // 2. 如果获取失败或树为空，初始化根目录
    if (treeResponse.code !== 0 || !treeResponse.data?.length) {
      const initResponse = await folderApi.initRootFolder()
      if (initResponse.code !== 0) {
        throw new Error(`初始化根目录失败: ${initResponse.message}`)
      }
      
      // 使用新建的根目录
      currentFolderId.value = initResponse.data.id
      currentFolderPath.value = [initResponse.data]
    } else {
      // 3. 正常情况使用获取到的树数据，选择第一个根文件夹
      const rootFolder = treeResponse.data[0]
      currentFolderId.value = rootFolder.id
      currentFolderPath.value = [rootFolder]
    }
    
    // 4. 加载根文件夹内容
    await loadFolderContents(currentFolderId.value)
  } catch (error) {
    console.error('初始化文件树失败:', error)
    ElMessage.error('初始化文件树失败')
  }
}

// 文件树相关方法
const loadFolderContents = async (folderId: number) => {
  try {
    const response = await folderApi.getContents(folderId)
    if (response.code !== 0) {
      throw new Error(response.message)
    }
    currentFolderContents.value = response.data
    currentFolderId.value = folderId
    
    // 构建树形数据
    buildFileTreeData()
  } catch (error) {
    console.error('加载文件夹内容失败:', error)
    ElMessage.error('加载文件夹内容失败')
  }
}

// 构建树形数据
const buildFileTreeData = () => {
  const treeData: any[] = []
  
  // 添加文件夹节点
  currentFolderContents.value.folders.forEach(folder => {
    const folderNode: any = {
      id: `folder-${folder.id}`,
      label: folder.folderName,
      isFolder: true,
      folderData: folder,
      isLeaf: false // 明确标记为非叶子节点，在懒加载模式下显示展开图标
    }
    treeData.push(folderNode)
  })
  
  // 添加文件节点
  currentFolderContents.value.files.forEach(file => {
    treeData.push({
      id: `file-${file.id}`,
      label: file.fileName,
      isFolder: false,
      fileData: file,
      isLeaf: true // 明确标记为叶子节点
    })
  })
  
  fileTreeData.value = treeData
}

// 处理树形节点选择
const handleTreeCheck = (checkedData: any, { checkedKeys, checkedNodes }: any) => {
  // 清空当前选择
  selectedFiles.value = []
  selectedFolders.value = []
  selectedFileIds.value.clear()
  selectedFolderIds.value.clear()
  
  // 重新构建选择列表
  checkedNodes.forEach((node: any) => {
    if (node.isFolder && node.folderData) {
      selectedFolders.value.push(node.folderData)
      selectedFolderIds.value.add(node.folderData.id)
    } else if (!node.isFolder && node.fileData) {
      selectedFiles.value.push(node.fileData)
      selectedFileIds.value.add(node.fileData.id)
    }
  })
}

// 懒加载树节点
const loadTreeNode = async (node: any, resolve: any) => {
  if (node.level === 0) {
    // 根节点，返回初始数据
    resolve(fileTreeData.value)
    return
  }
  
  if (node.data.isFolder) {
    try {
      const response = await folderApi.getContents(node.data.folderData.id)
      if (response.code === 0) {
        const children: any[] = []
        
        // 添加子文件夹
        response.data.folders.forEach((folder: FolderType) => {
          children.push({
            id: `folder-${folder.id}`,
            label: folder.folderName,
            isFolder: true,
            folderData: folder,
            isLeaf: false
          })
        })
        
        // 添加文件
        response.data.files.forEach((file: MyFile) => {
          children.push({
            id: `file-${file.id}`,
            label: file.fileName,
            isFolder: false,
            fileData: file,
            isLeaf: true
          })
        })
        
        resolve(children)
      } else {
        console.error('加载子文件夹失败:', response.message)
        ElMessage.error('加载子文件夹失败')
        resolve([])
      }
    } catch (error) {
      console.error('加载子文件夹失败:', error)
      ElMessage.error('加载子文件夹失败')
      resolve([])
    }
  } else {
    resolve([])
  }
}

// 处理树形节点点击（展开文件夹）
const handleTreeNodeClick = async (nodeData: any, node: any) => {
  // 懒加载模式下，不需要手动处理加载逻辑
  // el-tree 会自动调用 loadTreeNode 方法
}

// 获取节点图标
const getNodeIcon = (data: any) => {
  if (data.isFolder) {
    return Folder
  } else {
    return getFileIcon(data.label)
  }
}

// 获取节点颜色
const getNodeColor = (data: any) => {
  if (data.isFolder) {
    return '#409EFF'
  } else {
    return getFileColor(data.label)
  }
}

// 获取选择数量统计
const getSelectedCount = () => {
  return {
    files: selectedFiles.value.length,
    folders: selectedFolders.value.length
  }
}

// 清空树形选择
const clearTreeSelection = () => {
  if (fileTreeRef.value) {
    fileTreeRef.value.setCheckedKeys([])
  }
  clearSelection()
}

const enterFolder = async (folderId: number) => {
  try {
    // 获取当前文件夹信息用于构建路径
    const folder = currentFolderContents.value.folders.find(f => f.id === folderId)
    if (folder) {
      // 添加到路径
      currentFolderPath.value.push(folder)
      // 加载子文件夹内容
      await loadFolderContents(folderId)
    }
  } catch (error) {
    console.error('进入文件夹失败:', error)
    ElMessage.error('进入文件夹失败')
  }
}

const navigateToFolder = async (level: number) => {
  if (currentFolderPath.value.length <= level) return
  
  const targetIndex = currentFolderPath.value.length - 1 - level
  const targetFolder = currentFolderPath.value[targetIndex]
  
  if (targetFolder) {
    // 截断路径到目标位置
    currentFolderPath.value = currentFolderPath.value.slice(0, targetIndex + 1)
    await loadFolderContents(targetFolder.id)
  }
}

const toggleFileSidebar = () => {
  isFileSidebarCollapsed.value = !isFileSidebarCollapsed.value
}

const removeSelectedFile = (fileId: number) => {
  selectedFileIds.value.delete(fileId)
  selectedFiles.value = selectedFiles.value.filter(f => f.id !== fileId)
  
  // 同步更新树形选择状态
  if (fileTreeRef.value) {
    const currentChecked = fileTreeRef.value.getCheckedKeys()
    const newChecked = currentChecked.filter((key: string) => key !== `file-${fileId}`)
    fileTreeRef.value.setCheckedKeys(newChecked)
  }
}

const removeSelectedFolder = (folderId: number) => {
  selectedFolderIds.value.delete(folderId)
  selectedFolders.value = selectedFolders.value.filter(f => f.id !== folderId)
  
  // 同步更新树形选择状态
  if (fileTreeRef.value) {
    const currentChecked = fileTreeRef.value.getCheckedKeys()
    const newChecked = currentChecked.filter((key: string) => key !== `folder-${folderId}`)
    fileTreeRef.value.setCheckedKeys(newChecked)
  }
}

const clearSelection = () => {
  selectedFileIds.value.clear()
  selectedFolderIds.value.clear()
  selectedFiles.value = []
  selectedFolders.value = []
}

// 获取文件图标
const getFileIcon = (fileName: string) => {
  const extension = fileName.split('.').pop()?.toLowerCase()
  switch(extension) {
    case 'pdf':
    case 'doc':
    case 'docx':
    case 'xls':
    case 'xlsx':
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

const formatTime = (timeString: string) => {
  return format(new Date(timeString), 'HH:mm')
}

const sendMessage = async () => {
  const trimmedMessage = userInput.value.trim()
  if (!trimmedMessage || isSending.value) return

  isSending.value = true

  try {
    if (!chatStore.currentSessionId) {
      const sessionName = trimmedMessage.slice(0, 5) + (trimmedMessage.length > 5 ? '...' : '')
      await chatStore.createNewSession(sessionName)
    }

    // 构建请求数据
    const messageData = {
      content: trimmedMessage,
      fileIds: selectedFiles.value.length > 0 ? selectedFiles.value.map(f => f.id) : undefined,
      folderIds: selectedFolders.value.length > 0 ? selectedFolders.value.map(f => f.id) : undefined
    }

    await chatStore.sendNewMessageWithFiles(messageData)
    userInput.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    isSending.value = false
  }
}

const resetMessage = async () => {
  if (chatStore.currentSessionId) {
    chatStore.messages = []
    userInput.value = ''
  }
}

const createNewSession = async () => {
  const now = new Date()
  const timeString = [
    now.getFullYear(),
    String(now.getMonth() + 1).padStart(2, '0'),
    String(now.getDate()).padStart(2, '0'),
  ].join('') + '-' + [
    now.getHours().toString().padStart(2, '0'),
    now.getMinutes().toString().padStart(2, '0'),
    now.getSeconds().toString().padStart(2, '0')
  ].join('')

  await chatStore.createNewSession(`新对话 ${timeString}`)
  userInput.value = ''
}

const switchSession = async (sessionId: number) => {
  await chatStore.loadMessageHistory(sessionId)
  await nextTick()
  scrollToBottom()
}

const scrollToBottom = () => {
  if (chatWindow.value) {
    chatWindow.value.scrollTop = chatWindow.value.scrollHeight
  }
}

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

const copyMessage = async (text: string) => {
  try {
    const plainText = text.replace(/<[^>]*>?/gm, '')
    await navigator.clipboard.writeText(plainText)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
    ElMessage.error('复制失败')
  }
}

const editMessage = (index: number) => {
  const message = chatStore.messages[index]
  userInput.value = message.content.replace(/<[^>]*>?/gm, '')
  
  const inputElement = document.querySelector('.message-input textarea') as HTMLElement
  if (inputElement) {
    inputElement.focus()
  }
  
  chatStore.messages = chatStore.messages.slice(0, index)
}

const editingSessionId = ref<number | null>(null)
const editingSessionName = ref('')
const sessionNameInput = ref<HTMLInputElement | null>(null)

const startRenameSession = async (sessionId: number) => {
  editingSessionId.value = sessionId
  const session = chatStore.sessions.find(s => s.id === sessionId)
  if (session) {
    editingSessionName.value = session.sessionName
    // 等待DOM更新后聚焦输入框
    await nextTick()
    const inputElement = document.querySelector('.session-name-input input') as HTMLInputElement
    if (inputElement) {
      inputElement.focus()
      inputElement.select() // 选中所有文本
    }
  }
}

const finishRenameSession = async () => {
  if (editingSessionId.value !== null && editingSessionName.value.trim() !== '') {
    try {
      await chatStore.renameSessionById(editingSessionId.value, editingSessionName.value.trim())
      ElMessage.success('会话重命名成功')
    } catch (error) {
      console.error('重命名失败:', error)
      ElMessage.error('重命名失败')
    }
  }
  editingSessionId.value = null
  editingSessionName.value = ''
}

const cancelRenameSession = () => {
  editingSessionId.value = null
  editingSessionName.value = ''
}

const deleteCurrentSession = async () => {
  if (chatStore.currentSessionId) {
    try {
      await chatStore.deleteSessionById(chatStore.currentSessionId)
      ElMessage.success('会话已删除')
    } catch (error) {
      console.error('删除会话失败:', error)
      ElMessage.error('删除会话失败')
    }
  }
}

const clearCurrentSessionHistory = async () => {
  if (chatStore.currentSessionId) {
    try {
      await chatStore.clearHistory(chatStore.currentSessionId)
      ElMessage.success('会话历史已清除')
    } catch (error) {
      console.error('清除历史失败:', error)
      ElMessage.error('清除历史失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-app-container {
  display: flex;
  height: 100vh;
  background-color: #f5f9ff;

  .sidebar {
    width: 280px;
    height: 100%;
    background-color: #e6f2ff;
    box-shadow: 2px 0 8px rgba(0, 120, 255, 0.1);
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;
    border-right: 1px solid #d9e7ff;

    &.sidebar-collapsed {
      width: 68px;
    }

    .sidebar-header {
      padding: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid #d0e1ff;

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #1a73e8;
        margin: 0;
      }

      .collapse-btn {
        background-color: #e6f2ff;
        border-color: #c2d9ff;
        color: #1a73e8;

        &:hover {
          background-color: #d0e1ff;
        }
      }
    }

    .session-list {
      flex: 1;
      overflow: hidden;
      padding: 8px;

      .session-item {
        display: flex;
        align-items: center;
        padding: 10px 12px;
        margin: 4px 0;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          background-color: #d0e1ff;
        }

        &.active {
          background-color: #b3d4ff;
          color: #0052d9;

          .session-icon {
            color: #0052d9;
          }
        }

        .session-icon {
          margin-right: 8px;
          color: #4d88ff;
        }

        .session-name {
          font-size: 14px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .session-name-collapsed {
          font-weight: bold;
          color: #1a73e8;
        }

        .session-name-input {
          width: 100%;
          margin-left: 8px;
        }
      }
    }

    .sidebar-footer {
      padding: 12px;
      border-top: 1px solid #d0e1ff;

      .new-chat-btn {
        width: 100%;
        background-color: #1a73e8;
        border-color: #1a73e8;
        color: white;
        font-weight: 500;

        &:hover {
          background-color: #1662c4;
          border-color: #1662c4;
        }
      }

      .new-chat-btn-collapsed {
        background-color: #1a73e8;
        border-color: #1a73e8;
        color: white;
      }
    }
  }

  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: #f5f9ff;
    overflow: hidden;

    .chat-container {
      flex: 1;
      display: flex;
      flex-direction: column;
      max-width: 1400px; /* 扩大最大宽度 */
      margin: 0 auto;
      width: 100%;
      height: 100%;

      .chat-window {
        flex: 1;
        overflow-y: auto;
        padding: 24px 15%;
        background-color: #f5f9ff;

        .empty-message {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;

          .welcome-message {
            text-align: center;
            color: #1a73e8;

            h2 {
              font-size: 28px;
              margin-bottom: 16px;
            }

            p {
              font-size: 16px;
              color: #4d88ff;
            }
          }
        }

        .message-list {
          display: flex;
          flex-direction: column;
          gap: 20px;
          width: 100%;
          padding-bottom: 20px;

          .message-container {
            display: flex;
            gap: 12px;
            width: 100%;

            &.user-message-container {
              flex-direction: row-reverse;
            }

            .message-avatar {
              flex-shrink: 0;
            }

            .message-content {
              max-width: 85%;
              min-width: 20%;
              width: fit-content;
              
              padding: 12px 16px;
              border-radius: 12px;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
              transition: all 0.2s ease;

              &:hover {
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
              }

              &.user-message {
                background-color: #e6f2ff;
                border: 1px solid #c2d9ff;
              }

              &.bot-message {
                background-color: white;
                border: 1px solid #e0e9ff;
                @media (min-width: 1200px) {
                  width: min(85%, 1000px); /* 在宽屏下限制最大宽度 */
                }
              }

              .message-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 8px;

                .message-role {
                  font-size: 14px;
                  font-weight: 600;
                  color: #1a73e8;
                }

                .message-actions {
                  display: flex;
                  gap: 4px;

                  .action-btn {
                    color: #7aa6ff;

                    &:hover {
                      color: #1a73e8;
                    }
                  }
                }
              }

              .message-text {
                word-wrap: break-word;
                overflow-wrap: break-word;
                font-size: 15px;
                line-height: 1.6;
                color: #333;

                :deep(code) {
                  background-color: #f0f5ff;
                  border-radius: 4px;
                  padding: 2px 6px;
                  font-family: 'Fira Code', monospace;
                  font-size: 13px;
                  color: #1a73e8;
                }

                :deep(pre) {
                  background-color: #f0f5ff;
                  border-radius: 8px;
                  padding: 12px;
                  margin: 8px 0;
                  overflow-x: auto;
                }
              }

              .message-footer {
                margin-top: 8px;
                text-align: right;

                .message-time {
                  font-size: 12px;
                  color: #7aa6ff;
                }
              }
            }
          }
        }
      }

      .input-container {
        padding: 16px 15%; /* 与聊天区域对齐 */
        // background-color: white;
        // border-top: 1px solid #e0e9ff;
        box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.02);

        .selected-files-container {
          margin-bottom: 12px;
          padding: 12px;
          background-color: #f0f5ff;
          border-radius: 8px;
          border: 1px solid #d0e1ff;

          .selected-files-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            font-size: 14px;
            color: #666;
          }

          .selected-files-list {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;

            .selected-item {
              display: flex;
              align-items: center;
              gap: 4px;
              
              .el-icon {
                font-size: 12px;
              }
            }
          }
        }

        .message-input {
          :deep(.el-textarea__inner) {
            border-radius: 16px; /* 增加圆角 */
            border: 1px solid #d0e1ff;
            padding: 14px 18px; /* 增加内边距 */
            font-size: 15px;
            line-height: 1.6;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05); /* 更柔和的阴影 */
            transition: all 0.3s ease;
            min-height: 60px; /* 增加最小高度 */

            &:focus {
              border-color: #1a73e8;
              box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.2), 
                          0 4px 12px rgba(0, 0, 0, 0.08); /* 聚焦时更强的阴影 */
            }
          }

          .send-btn {
            height: 100%;
            padding: 0 20px;
            background-color: #1a73e8;
            border-color: #1a73e8;
            color: white;
            font-weight: 500;

            &:hover {
              background-color: #1662c4;
              border-color: #1662c4;
            }
          }
        }

        .action-buttons {
          margin-top: 12px;
          display: flex;
          justify-content: flex-end;
          gap: 12px;

          .delete-session-btn {
            border-radius: 12px;
            padding: 8px 16px;
            color: #f56565;
            border-color: #fed7d7;
            background-color: #fef2f2;
            transition: all 0.3s ease;

            &:hover {
              color: white;
              background-color: #f56565;
              border-color: #f56565;
              transform: translateY(-1px);
            }

            &:disabled {
              opacity: 0.5;
              cursor: not-allowed;
              
              &:hover {
                color: #f56565;
                background-color: #fef2f2;
                border-color: #fed7d7;
                transform: none;
              }
            }
          }

          .reset-btn {
            border-radius: 12px;
            padding: 8px 16px;
            color: #7aa6ff;
            border-color: #d0e1ff;
            transition: all 0.3s ease;

            &:hover {
              color: #1a73e8;
              border-color: #1a73e8;
              transform: translateY(-1px);
            }

            &:disabled {
              opacity: 0.5;
              cursor: not-allowed;
              
              &:hover {
                color: #7aa6ff;
                border-color: #d0e1ff;
                transform: none;
              }
            }
          }
        }
      }
    }
  }

  .file-sidebar {
    width: 300px;
    height: 100vh;
    background-color: #f8fafe;
    border-left: 1px solid #e0e9ff;
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;

    &.file-sidebar-collapsed {
      width: 60px;
    }

    .file-sidebar-header {
      padding: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #e0e9ff;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1a73e8;
      }

      .collapse-btn {
        background-color: #f8fafe;
        border-color: #e0e9ff;
        color: #1a73e8;

        &:hover {
          background-color: #e6f2ff;
        }
      }
    }

    .file-content {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .file-tree-container {
      flex: 1;
      overflow: auto;
      padding: 8px;
    }

    .file-tree {
      background-color: transparent;
      
      :deep(.el-tree-node__content) {
        height: 32px;
        border-radius: 4px;
        transition: all 0.2s ease;
        
        &:hover {
          background-color: #e6f2ff;
        }
      }
      
      :deep(.el-tree-node__expand-icon) {
        color: #1a73e8;
        font-size: 14px;
      }
      
      :deep(.el-checkbox) {
        margin-right: 8px;
      }
      
      .tree-node {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        padding-right: 8px;
        
        .node-content {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;
          min-width: 0;

          .node-icon {
            flex-shrink: 0;
            font-size: 16px;
          }

          .node-label {
            font-size: 13px;
            font-weight: 500;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .node-meta {
          flex-shrink: 0;
          
          .node-type {
            font-size: 11px;
            color: #999;
          }
        }
      }
    }

    .selection-summary {
      padding: 12px 16px;
      border-top: 1px solid #e0e9ff;
      background-color: #f0f5ff;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .summary-text {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

@media (max-width: 1600px) {
  .main-content .chat-container {
    max-width: 1200px;
    
    .chat-window {
      padding: 24px 10%;
      
      .message-list .message-container {
        .message-content.bot-message {
          width: min(90%, 900px);
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .main-content .chat-container {
    max-width: 100%;
    
    .chat-window {
      padding: 24px 8%;
      
      .message-list .message-container {
        .message-content {
          max-width: 90%;
          
          &.bot-message {
            width: min(95%, 100%);
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .chat-app-container {
    flex-direction: column;

    .sidebar {
      width: 100%;
      height: auto;
      max-height: 200px;
      
      &.sidebar-collapsed {
        width: 100%;
        max-height: 60px;
      }
    }

    .file-sidebar {
      width: 100%;
      height: 40vh;
      border-left: none;
      border-top: 1px solid #e0e9ff;
      order: 3;
      
      &.file-sidebar-collapsed {
        height: 60px;
      }
    }

    .main-content {
      order: 2;
      
      .chat-container {
        .chat-window, .input-container {
          padding: 16px;
        }

        .message-content {
          max-width: 90% !important;
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .chat-app-container .main-content .chat-container {
    .chat-window, .input-container {
      padding-left: 15%;
      padding-right: 15%;
    }
  }
}

@media (max-width: 992px) {
  .chat-app-container .main-content .chat-container {
    .chat-window, .input-container {
      padding-left: 10%;
      padding-right: 10%;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-container {
  animation: fadeIn 0.3s ease;
}

.code-block {
  margin: 12px 0;
  background: #e6f2ff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #c2d9ff;

  .code-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: #d0e1ff;
    border-bottom: 1px solid #b3d4ff;

    .language {
      color: #1a73e8;
      font-size: 12px;
      font-weight: 500;
    }

    .copy-btn {
      color: #1a73e8;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
      
      &:hover {
        color: #0052d9;
      }
      
      &.copied {
        color: #34a853;
      }
    }
  }

  pre {
    margin: 0;
    padding: 12px;
    background: #f0f5ff !important;
    
    code {
      font-family: 'Fira Code', monospace;
      font-size: 14px;
      line-height: 1.5;
      color: #1a73e8;
    }
  }
}
</style>