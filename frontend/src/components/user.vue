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
        <!-- background -->
        <el-image style="animation: header-effect 2s" class="background-image-index" v-once lazy :src="bgImage"
          fit="cover">
          <div slot="error" class="image-slot background-image-index-error"></div>
        </el-image>
        <!-- word -->
        <div class="signature-wall myCenter my-animation-hideToShow">
          <h1 class="playful">
            <span v-for="(char, index) in webTitle" :key="index">{{ char }}</span>
          </h1>
          <div class="printer">
            <Printer :printerInfo="printerInfo.content">
              <template #paper="{ content }">
                <h3>
                  {{ content }}<span class="cursor">|</span>
                </h3>
              </template>
            </Printer>
          </div>
          <div id="bannerWave1"></div>
          <div id="bannerWave2"></div>
        </div>
        <!-- content -->
        <div class="page-container-wrap">
          <div class="page-container">
            <div class="recent-posts">
              <div class="announcement background-opacity">
                <i class="fa fa-volume-up" aria-hidden="true"></i>
                <div>
                  hhhhhhhhhhhhhhhhhh
                </div>
              </div>
              <div style="padding: 0 20px">
                <CarAnimation />
              </div>
              <div>
                <ResourceCardList :resourcePathList="resourcePathList" @clickResourcePath="handleResourceClick" />
              </div>
            </div>
          </div>
        </div>
        <!-- myFooter -->
        <div style="background: var(--background)">
          <myFooter :showFooter="true"></myFooter>
        </div>
      </template>
    </loader>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import myFooter from '@/components/common/myFooter.vue';
import loader from '@/components/common/loader.vue';
import zombie from '@/components/common/zombie.vue';
import bgImage from '@/assets/bgimg.png';
import CarAnimation from '@/components/common/CarAnimation.vue';
import Printer from '@/components/common/printer.vue';
import { useRouter } from 'vue-router';
// 新增 `ResourceCardList` 组件导入
import ResourceCardList from '@/components/common/ResourceCardList.vue';

const loading = ref(false);
const router = useRouter();
// 模拟 webTitle 数据
const webTitle = ref('心灵捕手——在乎每颗心'.split(''));
// printerInfo 数据
const printerInfo = ref({
  content: '多模态与单样本双方法切割全心脏，MedicalSeg-Vnet检测心肌瘢痕'
});
// 在组件挂载时调用函数
onMounted(() => {
  //初始化
});
const filesInfo = ref([
  {
    id: 1,
    url: 'https://aircraft-carrier.github.io/images/avatar.jpg',
    fileName: 'avatar'
  },
  {
    id: 2,
    url: 'https://q4.itc.cn/q_70/images03/20240818/2d435ba0937d4b81a1654a209f3006f0.png',
    fileName: 'nida'
  },
  {
    id: 3,
    url: 'https://www.acgdh.cc/wp-content/uploads/2023/10/10-23-3.jpg',
    fileName: 'pz'
  },
  {
    id: 4,
    url: "https://camo.githubusercontent.com/878df94c82df67088b79330aa32ac7b256b668736d5cbfdb609247ccd9f125c5/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f41697263726166742d636172726965722f506963474f4f2f696d616765732f62362e6a7067",
    fileName: "sakana"
  },
  {
    id: 5,
    url: "https://www.acgdh.cc/wp-content/uploads/2023/10/10-21-3.jpg",
    fileName: "acgdh"
  },
  {
    id: 6,
    url: "https://img2.huashi6.com/images/resource/2020/12/18/863h69435p0.jpg?imageMogr2/quality/100/interlace/1/thumbnail/2000x%3E",
    fileName: "pic"
  }
]);
const resourcePathList = computed(() => {
  return filesInfo.value.map((info, index) => ({
    cover: info.url ? info.url : 'https://www.acgdh.cc/wp-content/uploads/2023/10/10-23-3.jpg',
    // cover: info.url,
    title: info.fileName,
    introduction: '一个 nii 图像',
    recommendStatus: true,
    createTime: '2024-08-15T00:00:00Z',
    url: '/chat',
    id: info.id
  }))
})
async function handleResourceClick(id) {
  router.push('/chat');
}
</script>
<style scoped>
.edit-area {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-top: 10px;
  background-color: #f9f9f9;
}

.input-item {
  margin-bottom: 10px;
}

.el-col {
  padding-bottom: 10px;
}

.upload-center {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  /* 根据需要调整高度 */
}

.card {
  border: 1px solid #ebeef5;
  padding: 14px;
  margin-top: 10px;
  border-radius: 4px;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 主体内容 */
.container {
  width: 100%;
  /* 可以根据需要调整 */
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  width: 90%;
  padding: 0 20px 40px 20px;
  margin: 0 auto;
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
  margin: 40px auto;
  align-items: center;
  justify-content: center;
  /* 使内容在主轴上居中 */
}

.announcement i {
  color: var(--themeBackground);
  font-size: 22px;
  margin-right: 20px;
  /* 右边留出空间 */
  animation: scale 0.8s ease-in-out infinite;
}

.announcement div {
  line-height: 30px;
  text-align: center;
  /* 使文本在div中居中 */
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

.dialog-footer {
  text-align: right;
}

.cd-top {
  background: var(--toTop) no-repeat center;
  position: fixed;
  right: 5vh;
  top: -900px;
  z-index: 99;
  width: 70px;
  height: 900px;
  background-size: contain;
  transition: all 0.5s ease-in-out;
  cursor: pointer;
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
