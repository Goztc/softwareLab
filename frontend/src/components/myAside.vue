<template>
  <div>
    <div class="myAside-container ">

    <div
       style="padding: 15px; border-radius: 10px; animation: hideToShow 1s ease-in-out;margin-top: 50px;"
       class="shadow-box background-opacity wow">
    <div style="color: var(--lightGreen); font-size: 20px; font-weight: bold; margin-bottom: 10px" 
         @click="showWenxin()">
      文心智能助手
    </div>

    <!-- 问答功能部分 -->
    <div class="chat-window" style="max-height: 300px; overflow-y: auto; margin-bottom: 10px;">
      <div class="message-list">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message" 
          :class="{ 'user-message': msg.isUser }"
        >  
          <span v-if="msg.isUser">{{ msg.text }}</span>  
          <span v-else v-html="msg.text"></span>
        </div>
      </div>
    </div>

    <div style="display: flex; margin-bottom: 10px;">
      <input class="ais-SearchBox-input" type="text"
             v-model="userInput"
             placeholder="输入消息 ......" 
             maxlength="32" 
             @keyup.enter="sendMessage"
             style="flex: 1;">
      <div class="ais-SearchBox-submit" @click="sendMessage">
        <svg style="margin-top: 3.5px; margin-left: 18px" viewBox="0 0 1024 1024" width="20" height="20">
          <path
            d="M51.2 508.8c0 256.8 208 464.8 464.8 464.8s464.8-208 464.8-464.8-208-464.8-464.8-464.8-464.8 208-464.8 464.8z"
            fill="#51C492"></path>
          <path
            d="M772.8 718.4c48-58.4 76.8-132.8 76.8-213.6 0-186.4-151.2-337.6-337.6-337.6-186.4 0-337.6 151.2-337.6 337.6 0 186.4 151.2 337.6 337.6 337.6 81.6 0 156-28.8 213.6-76.8L856 896l47.2-47.2-130.4-130.4zM512 776c-149.6 0-270.4-121.6-270.4-271.2S363.2 233.6 512 233.6c149.6 0 271.2 121.6 271.2 271.2C782.4 654.4 660.8 776 512 776z"
            fill="#FFFFFF"></path>
        </svg>
      </div>
    </div>
    
  </div>


    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted} from 'vue';
import { ChatService } from '@/api/wenxin';  
import { marked } from 'marked'; 
import { useStore } from 'vuex';

const store = useStore(); 
const visible = computed(() => store.state.visible);
const asideshow = computed(() => store.state.asideshow);

const toggleDropdown = (menuId) => {   
  if (openedMenus.value !== menuId) {   
    openedMenus.value = menuId;  
  }  
};  

const isMobile = computed(() => {
  return window.innerWidth <= 768;
});


onMounted(() => {
  // initThree();  
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
});



const userInput = ref('');  
const messages = ref([]);  

const sendMessage = async () => {  
  const trimmedMessage = userInput.value.trim();  
  if (trimmedMessage) {  
    messages.value.push({ text: trimmedMessage, isUser: true });  
    userInput.value = ''; 

    try {  
      const response = await ChatService({ question: trimmedMessage });  
      const responseMessage = response.data;  
      const parsedMessage = marked(responseMessage);  
      
      messages.value.push({ text: parsedMessage, isUser: false });  
    } catch (error) {  
      console.error('API 请求失败:', error);  
      messages.value.push({ text: '发送消息失败，请稍后再试。', isUser: false });  
    }  
  }  
};

</script>


<style scoped>
  /*wenxin*/
  .chat-window {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 10px;
}

.message-list {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.message {
  padding: 8px;
  margin: 5px 0;
  border-radius: 4px;
  max-width: 100%;
  word-wrap: break-word;
}

.user-message {
  background-color: #d1e7dd;
  align-self: flex-end;
}
/*wenxin over*/

  .myAside-container > div:not(:last-child) {
    margin-bottom: 30px;
  }


  .ais-SearchBox-input {
    padding: 0 14px;
    height: 30px;
    width: calc(100% - 50px);
    outline: 0;
    border: 2px solid var(--lightGreen);
    border-right: 0;
    border-radius: 40px 0 0 40px;
    color: var(--maxGreyFont);
    background: var(--white);
  }

  .ais-SearchBox-submit {
    height: 30px;
    width: 50px;
    border: 2px solid var(--lightGreen);
    border-left: 0;
    border-radius: 0 40px 40px 0;
    background: var(--white);
    cursor: pointer;
  }

</style>