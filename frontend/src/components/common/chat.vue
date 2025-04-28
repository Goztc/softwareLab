<template>  
  <div class="chat-container">  
    <div class="chat-window">  
      <div class="message-list">  
        <div   
          v-for="msg in messages"   
          :key="msg.id"   
          class="message"   
          :class="{ 'user-message': msg.isUser }"  
        >  
          <span v-if="msg.isUser">{{ msg.text }}</span>  
          <span v-else v-html="msg.text"></span>   
        </div>  
      </div>  
    </div>  
    <div class="input-container">  
      <input   
        v-model="userInput"   
        @keyup.enter="sendMessage"   
        placeholder="输入你的消息..."  
      />  
      <button @click="sendMessage">发送</button>   
      <button @click="resetMessage">清空对话</button>  
    </div>  
  </div>  
</template>  

<script setup>  
import { ref } from 'vue';  
import { ChatService, ChatBotService, ChatBotResetService } from '@/api/wenxin';  
import { marked } from 'marked';   

const userInput = ref('');  
const messages = ref([]);  

const sendMessage = async () => {  
  const trimmedMessage = userInput.value.trim();  
  if (trimmedMessage) {  
    const messageId = Date.now(); // 生成唯一 ID  
    messages.value.push({ id: messageId, text: trimmedMessage, isUser: true });  
    userInput.value = '';  

    try {  
      const response = await ChatBotService({ question: trimmedMessage });  
      const responseMessage = response.data;  
      const parsedMessage = marked(responseMessage);  
      messages.value.push({ id: Date.now(), text: parsedMessage, isUser: false });  
    } catch (error) {  
      console.error('API 请求失败:', error);  
      messages.value.push({ id: Date.now(), text: '发送消息失败，请稍后再试。', isUser: false });  
    }  
  }  
};  

const resetMessage = async () => {  
  messages.value = [];  
  userInput.value = '';  

  try {   
    await ChatBotResetService();  
  } catch (error) {  
    console.error('API 请求失败:', error);  
  }  
}  
</script>  

<style scoped>  
.chat-container {  
  width: 100%;  
  max-width: 800px;  
  margin: auto;  
  padding: 20px;  
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);  
  height: 500px;  
  border: 1px solid #ddd;  
  border-radius: 8px;  
  display: flex;  
  flex-direction: column;  
}  

.chat-window {  
  flex: 1;  
  overflow-y: auto;  
  padding: 10px;  
}  

.message-list {  
  display: flex;  
  flex-direction: column;  
}  

.message {  
  padding: 8px;  
  margin: 5px 0;  
  border-radius: 4px;  
  max-width: 70%;  
  word-wrap: break-word;  
}  

.user-message {  
  background-color: #d1e7dd;  
  align-self: flex-end;  
}  

.input-container {  
  display: flex;  
  padding: 10px;  
}  

input {  
  flex: 1;  
  padding: 8px;  
  border: 1px solid #ddd;  
  border-radius: 4px;  
}  

button {  
  margin-left: 10px;  
  padding: 8px 12px;  
  background-color: #007bff;  
  color: #fff;  
  border: none;  
  border-radius: 4px;  
  cursor: pointer;  
}  
</style>