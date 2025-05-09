<!-- App.vue -->
<template>
  <div>
    <!-- loader -->
    <loader :loading="loading">


      <template #loader>
        <div>
          <zombie></zombie>
        </div>
      </template>
      <template #body>
        <!-- content -->
        <div class="page-container-wrap">
          <div class="page-container">

              <div class="container">
                  <Chat/>
              </div>
          </div>
        </div>
      </template>
    </loader>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import loader from '@/components/common/loader.vue';
import zombie from '@/components/common/zombie.vue';
import Chat from '@/components/chat/chat.vue'
import { useChatStore } from '@/stores/chat';


const chatStore = useChatStore();

onMounted(() => {
  chatStore.loadSessions();
});
</script>



<style scoped>
/* 主体内容 */
.container {
  width: 100%;
  /* 可以根据需要调整 */
  display: flex;
  flex-direction: column;
  /* 控制上下组件之间的间距 */
}

/* 主题内容 end */

.background-image-index {
  width: 100vw;
  height: 50vh;
  position: fixed;
  z-index: -1;
}

.background-image-index::before {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, .2);
  content: '';
}

.background-image-index-error {
  background-color: var(--lightGreen);
  width: 100vw;
  height: 50vh;
  position: fixed;
  z-index: -1;
}

.signature-wall {
  /* 向下排列 */
  display: flex;
  flex-direction: column;
  position: relative;
  user-select: none;
  height: 50vh;
  overflow: hidden;
}

.playful {
  color: var(--white);
  font-size: 40px;
}

.sort-article-first {
  margin: 40px auto 20px;
  display: flex;
  justify-content: space-between;
  color: var(--greyFont);
  border-bottom: 1px dashed var(--lightGray);
  padding-bottom: 5px;
}

.article-more {
  cursor: pointer;
  transition: all 0.3s;
}

.article-more:hover {
  color: var(--lightGreen);
  font-weight: 700;
  transform: scale(1.1);
}

.printer {
  cursor: pointer;
  color: var(--white);
  background: var(--translucent);
  border-radius: 10px;
  padding-left: 10px;
  padding-right: 10px;
}

#bannerWave1 {
  height: 84px;
  background: var(--bannerWave1);
  position: absolute;
  width: 200%;
  bottom: 0;
  z-index: 10;
  animation: gradientBG 120s linear infinite;
}

#bannerWave2 {
  height: 100px;
  background: var(--bannerWave2);
  position: absolute;
  width: 400%;
  bottom: 0;
  z-index: 5;
  animation: gradientBG 120s linear infinite;
}

/* 光标 */
.cursor {
  margin-left: 1px;
  animation: hideToShow 0.7s infinite;
  font-weight: 200;
}

.el-icon-arrow-down {
  font-size: 40px;
  font-weight: bold;
  color: var(--white);
  position: absolute;
  bottom: 60px;
  animation: my-shake 1.5s ease-out infinite;
  z-index: 15;
  cursor: pointer;
}

.page-container-wrap {
  background: var(--background);
  position: relative;
}

.page-container {
  display: flex;
  justify-content: center;
  width: 100%;
  /* padding: 0 20px; */
  /* margin: 0 auto; */
  flex-direction: row;
}

.recent-posts {
  width: 70%;
}

.announcement {
  padding: 22px;
  border: 1px dashed var(--lightGray);
  color: var(--greyFont);
  border-radius: 10px;
  display: flex;
  margin: 40px auto 40px;
}

.announcement i {
  color: var(--themeBackground);
  font-size: 22px;
  margin: auto 0;
  animation: scale 0.8s ease-in-out infinite;
}

.announcement div div {
  margin-left: 20px;
  line-height: 30px;
}

.aside-content {
  width: calc(30% - 40px);
  user-select: none;
  margin-top: 40px;
  margin-right: 40px;
  max-width: 300px;
  float: right;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.pagination {
  padding: 13px 15px;
  border: 1px solid var(--lightGray);
  border-radius: 3rem;
  color: var(--greyFont);
  width: 100px;
  user-select: none;
  cursor: pointer;
  text-align: center;
}

.pagination:hover {
  border: 1px solid var(--themeBackground);
  color: var(--themeBackground);
  box-shadow: 0 0 5px var(--themeBackground);
}

.push-title {
  font-weight: bold;
  font-size: 20px;
}

.push-el-image {
  width: 80%;
  min-height: 100px;
  max-height: 400px;
  border-radius: 15px;
  margin-top: 20px;
  margin-bottom: 30px;
}

.push-button {
  position: relative;
  background: var(--lightGreen);
  cursor: pointer;
  width: 230px;
  border-radius: 2rem;
  line-height: 35px;
  color: var(--white);
}

.push-button-title {
  margin-left: 20px;
  font-weight: bold;
}

.push-button-car {
  position: absolute;
  margin-left: 55px;
  animation: passing 4s linear infinite;
}

@media screen and (max-width: 1100px) {
  .recent-posts {
    width: 100%;
  }

  .page-container {
    width: 100%;
  }
}

@media screen and (max-width: 1000px) {

  .page-container {
    /* 文章栏与侧标栏垂直排列 */
    flex-direction: column;
  }

  .aside-content {
    width: 100%;
    max-width: unset;
    float: unset;
    margin: 40px auto 0;
  }
}

@media screen and (max-width: 768px) {

  h1 {
    font-size: 35px;
  }
}
</style>
