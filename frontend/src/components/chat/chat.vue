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
                
                <!-- 显示文档引用 -->
                <DocumentReference 
                  v-if="msg.role === 'assistant' && msg.sources && msg.sources.length > 0"
                  :sources="msg.sources"
                />
                
                <div class="message-footer">
                  <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
  
        <!-- 输入区域 -->
        <div class="input-container">
          <!-- RAG设置面板 -->
          <div class="rag-settings">
            <div class="rag-toggle">
              <el-switch
                v-model="chatStore.useRAG"
                size="small"
                active-text="RAG模式"
                inactive-text="普通聊天"
                active-color="#1a73e8"
              />
            </div>
            <div v-if="chatStore.useRAG" class="document-path-input">
              <el-input
                v-model="chatStore.documentPath"
                placeholder="文档路径 (如: ai, tech/ml.txt)"
                size="small"
                class="document-input"
                prefix-icon="Folder"
              />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { marked } from 'marked'
import { useChatStore } from '@/stores/chat'
import { ChatDotRound, Plus, Promotion, Delete, DocumentCopy, Edit } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { useDisplay } from 'vuetify'
import { ElMessage } from 'element-plus'
import CodeBlock from "@/components/chat/CodeBlock.vue"
import DocumentReference from "@/components/chat/DocumentReference.vue"

const chatStore = useChatStore()
const userInput = ref('')
const isSending = ref(false)
const isSidebarCollapsed = ref(false)
const chatWindow = ref<HTMLElement | null>(null)
const { mobile } = useDisplay()
const isMobile = computed(() => mobile.value)

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
})

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

    await chatStore.sendNewMessage(trimmedMessage)
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

        .rag-settings {
          margin-bottom: 12px;
          display: flex;
          align-items: center;
          gap: 12px;

          .rag-toggle {
            flex: 1;
          }

          .document-path-input {
            flex: 1;
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
    .main-content .chat-container {
      .chat-window {
        padding: 16px;
        
        .message-list .message-container {
          .message-content {
            max-width: 95%;
            min-width: 40%;
            
            &.bot-message, &.user-message {
              width: min(95%, 100%);
            }
          }
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

    .main-content .chat-container {
      .chat-window, .input-container {
        padding: 16px;
      }

      .message-content {
        max-width: 90% !important;
      }
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