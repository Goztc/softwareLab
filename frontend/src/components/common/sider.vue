<template>
  <!-- 可拖拽的聊天助手图标 -->
  <div class="chat-assistant" :style="{ left: `${position.x}px`, top: `${position.y}px` }">
    <!-- 助手图标 -->
    <img src="@/assets/chat-assistant.png" class="assistant-icon" @mousedown="startDrag" @click="toggleChat"
      alt="Chat Assistant" />

    <!-- 聊天窗口 -->
    <div v-if="showChat" class="chat-window">
      <div class="chat-header">
        <h3>AI 助手</h3>
        <button @click="toggleChat" class="close-button">×</button>
      </div>

      <!-- 消息区域 -->
      <div class="messages-container">
        <div v-for="(message, index) in messages" :key="index" :class="['message', message.sender]">
          <div class="message-content">{{ message.content }}</div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <input v-model="inputMessage" @keyup.enter="sendMessage" placeholder="输入您的问题..." />
        <button @click="sendMessage" class="send-button">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';

// 位置状态
const position = reactive({ x: 0, y: 0 });

// 聊天状态
const showChat = ref(false);
const inputMessage = ref('');
const messages = ref([
  {
    sender: 'assistant',
    content: '您好！我是AI助手，有什么可以帮您？',
    timestamp: Date.now()
  }
]);

// 初始化位置
onMounted(() => {
  position.x = window.innerWidth - 200;
  position.y = window.innerHeight - 200;
});

// 切换聊天窗口
const toggleChat = () => {
  showChat.value = !showChat.value;
};

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return;

  // 添加用户消息
  messages.value.push({
    sender: 'user',
    content: inputMessage.value,
    timestamp: Date.now()
  });

  // 模拟AI回复
  setTimeout(() => {
    messages.value.push({
      sender: 'assistant',
      content: `这是对"${inputMessage.value}"的回复`,
      timestamp: Date.now()
    });
  }, 800);

  inputMessage.value = '';
};

// 拖拽功能
const startDrag = (event) => {
  event.preventDefault();
  const startX = event.clientX - position.x;
  const startY = event.clientY - position.y;

  const onMouseMove = (e) => {
    position.x = e.clientX - startX;
    position.y = e.clientY - startY;
  };

  const onMouseUp = () => {
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
  };

  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', onMouseUp);
};

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};
</script>

<style scoped>
.chat-assistant {
  position: fixed;
  z-index: 9999;
  cursor: pointer;
}

.assistant-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}

.assistant-icon:hover {
  transform: scale(1.1);
}

.chat-window {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 300px;
  height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 12px 16px;
  background: #4a6fa5;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-button {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
}

.messages-container {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background: #f5f5f5;
}

.message {
  margin-bottom: 12px;
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 18px;
  word-wrap: break-word;
}

.message.user {
  margin-left: auto;
  background: #4a6fa5;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant {
  margin-right: auto;
  background: #e9e9e9;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-content {
  margin-bottom: 4px;
}

.message-time {
  font-size: 10px;
  color: #666;
  text-align: right;
}

.input-area {
  display: flex;
  padding: 10px;
  background: white;
  border-top: 1px solid #eee;
}

.input-area input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  outline: none;
}

.send-button {
  margin-left: 8px;
  padding: 8px 16px;
  background: #4a6fa5;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

.send-button:hover {
  background: #3a5a8f;
}
</style>