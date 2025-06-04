<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import {
  User,
  Crop,
  SwitchButton,
} from '@element-plus/icons-vue'


// # 变量声明
const toolButton = ref(false);
const hoverEnter = ref(false);
const isDark = ref(false);
const scrollTop = ref(false);
const toolbarDrawer = ref(false);
const router = useRouter();
const store = useStore();


const toolbar = computed(() => store.state.toolbar);
const visiblecontrol = computed(() => store.state.visible);
import Sider from '@/components/common/sider.vue'
// # function
// 改变颜色
const changeColor = () => {
  isDark.value = !isDark.value;
  let root = document.querySelector(":root");

  if (isDark.value) {
    root.style.setProperty("--background", "#272727");
    root.style.setProperty("--fontColor", "white");
    root.style.setProperty("--borderColor", "#4F4F4F");
    root.style.setProperty("--borderHoverColor", "black");
    root.style.setProperty("--articleFontColor", "#E4E4E4");
    root.style.setProperty("--articleGreyFontColor", "#D4D4D4");
    root.style.setProperty("--commentContent", "#D4D4D4");
    root.style.setProperty("--favoriteBg", "#1e1e1e");
  } else {
    root.style.setProperty("--background", "white");
    root.style.setProperty("--fontColor", "black");
    root.style.setProperty("--borderColor", "rgba(0, 0, 0, 0.5)");
    root.style.setProperty("--borderHoverColor", "rgba(110, 110, 110, 0.4)");
    root.style.setProperty("--articleFontColor", "#1F1F1F");
    root.style.setProperty("--articleGreyFontColor", "#616161");
    root.style.setProperty("--commentContent", "#F7F9FE");
    root.style.setProperty("--favoriteBg", "#f7f9fe");
  }
};

// 回到顶部
const toTop = () => {
  window.scrollTo({
    top: 0,
    behavior: "smooth"
  });
};

const onScrollPage = () => {
  scrollTop.value = document.documentElement.scrollTop || document.body.scrollTop;
};

// 判断是否是白天
const isDaylight = () => {
  let currDate = new Date();
  return currDate.getHours() > 22 || currDate.getHours() < 7;
};

// 初始化导航栏状态并提交到 Vuex store
const initializeStatus = () => {
  const toolbarStatus = {
    enter: false,
    visible: true,
  };
  store.commit('changeToolbarStatus', toolbarStatus);
};

// 更换路由
const smallMenu = (data) => {
  router.push(data);
  toolbarDrawer.value = false;
};


// # 生命周期
// onMounted 在组件被挂载到 DOM（文档对象模型）之后执行一些代码。
onMounted(() => {
  initializeStatus();
  // 监听窗口的滚动事件，绑定 onScrollPage 方法
  window.addEventListener("scroll", onScrollPage);

  // 设置页面主题
  if (isDaylight()) {
    isDark.value = true;
    let root = document.querySelector(":root");
    root.style.setProperty("--background", "#272727");
    root.style.setProperty("--fontColor", "white");
    root.style.setProperty("--borderColor", "#4F4F4F");
    root.style.setProperty("--commentContent", "#D4D4D4");
    root.style.setProperty("--favoriteBg", "#1e1e1e");
  }
});

// onUnmounted 在组件将从 DOM 中移除的时候运行的代码
onUnmounted(() => {
  window.removeEventListener("scroll", onScrollPage);
});

// 观察 scrollTop 变化
// watch 用于观察一个或多个响应式数据源，并在这些数据源发生变化时执行回调函数。
watch(scrollTop, (newScrollTop, oldScrollTop) => {
  // 如果滑动距离超过屏幕高度的一半视为进入页面，背景改为白色
  let enter = newScrollTop > window.innerHeight / 2;
  const top = newScrollTop - oldScrollTop < 0;  // 向下滑的时候不显示导航栏，向上划的时候显示，初始状态显示
  let isShow = newScrollTop - window.innerHeight > 30;
  toolButton.value = isShow;

  // 动态调整 css 样式
  const cdTopElement = document.querySelector('.cd-top');

  if (cdTopElement) {
    if (isShow) {
      if (window.innerHeight > 950) {
        cdTopElement.style.top = '0';
      } else {
        cdTopElement.style.top = `${window.innerHeight - 950}px`;
      }
    } else {
      cdTopElement.style.top = '-900px';
    }
  } else {
    console.warn("Element with class 'cd-top' not found.");
  }

  // 导航栏显示与颜色
  let toolbarStatus = {
    enter: enter,
    // visible: top,
    visible: true,
  };
  store.commit('changeToolbarStatus', toolbarStatus);
});

import useUserInfoStore from '@/stores/userInfo.js'
import { useTokenStore } from '@/stores/token.js'
import { useChatStore } from '@/stores/chat.ts'
import { useFileStore } from '@/stores/file.ts'
const tokenStore = useTokenStore();
const userInfoStore = useUserInfoStore();
const chatStore = useChatStore()
const fileStore = useFileStore()

import { ElMessage, ElMessageBox } from 'element-plus'
const handleCommand = (command)=>{
    //判断指令
    if(command === 'logout'){
        //退出登录
        ElMessageBox.confirm(
        '您确认要退出吗?',
        '温馨提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            //退出登录
            //1.清空pinia中存储的token以及个人信息
            tokenStore.removeToken()
            userInfoStore.removeInfo()
            fileStore.reset();
            chatStore.reset();
            //2.跳转到登录页面
            router.push('/login')
            ElMessage({
                type: 'success',
                message: '退出登录成功',
            })
            
        })
        .catch(() => {
            ElMessage({
                type: 'info',
                message: '用户取消了退出登录',
            })
        })
    }else if(command === 'avatar'){
        ElMessage({
            type: 'success',
            message: 'avatar',
        })
        store.commit('changeVisibility', { avatar: !visiblecontrol.value.avatar });
    }else if(command === 'info'){
        ElMessage({
            type: 'success',
            message: 'info',
        })
        store.commit('changeVisibility', { info: !visiblecontrol.value.info });
    }
}
</script>


<template>
  <div>
    <!-- el过渡动画 -->
    <transition name="el-fade-in-linear">
      <!-- 导航栏 -->
      <div v-show="toolbar.visible" @mouseenter="hoverEnter = true" @mouseleave="hoverEnter = false"
        :class="[{ enter: toolbar.enter }, { hoverEnter: hoverEnter && !toolbar.enter }]"
        class="toolbar-content myBetween">
        <div class="toolbar-title">
          <h2 @click="router.push({ path: '/login' })"> Aircraft </h2>
        </div>

        <!-- 导航列表 -->
        <div>
          <ul class="scroll-menu">
            <li @click="router.push({ path: '/home' })">
              <div class="my-menu">
                🏡 <span>首页</span>
              </div>
            </li>
            
            <li @click="router.push({ path: '/file' })">
              <div class="my-menu">
                🏡 <span>文件管理</span>
              </div>
            </li>

            <li @click="router.push({ path: '/chat' })">
              <div class="my-menu">
                🤖 <span>chat</span>
              </div>
            </li>

            <el-dropdown :hide-timeout="500" placement="bottom-end" @command="handleCommand">
              <li>
                <div class="my-menu">
                  📝 <span>个人中心</span>
                </div>
              </li>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="info" :icon="User">基本资料</el-dropdown-item>
                  <el-dropdown-item command="avatar" :icon="Crop">更换头像</el-dropdown-item>
                  <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

          </ul>
        </div>
      </div>
    </transition>

    <div id="main-container">
      <router-view></router-view>
    </div>


    <div class="toolButton">
      <div class="backTop" @click="toTop()">
        <!-- 回到顶部按钮 -->
        <svg viewBox="0 0 1024 1024" width="50" height="50">
          <path
            d="M696.741825 447.714002c2.717387-214.485615-173.757803-312.227566-187.33574-320.371729-10.857551 5.430775-190.050127 103.168727-187.33274 320.371729-35.297037 24.435488-73.306463 65.1623-67.875688 135.752376 5.430775 70.589076 76.018851 119.460051 103.168726 116.745664 27.152875-2.716387 19.004713-21.7221 19.004713-21.7221l8.148162-38.011425s40.721814 59.732525 51.583363 59.732525h146.609927c13.574938 0 51.585363-59.732525 51.585363-59.732525l8.147162 38.011425s-8.147162 19.005713 19.004713 21.7221c27.148876 2.714388 97.738951-46.156588 103.168727-116.745664s-32.57965-111.316888-67.876688-135.752376z m-187.33574-2.713388c-5.426776 0-70.589076-2.717387-78.733239-78.737238 2.713388-73.306463 73.306463-78.733239 78.733239-81.450626 5.430775 0 76.02385 8.144163 78.736238 81.450626-8.143163 76.019851-73.305463 78.737238-78.736238 78.737238z m0 0"
            fill="#000000"></path>
          <path
            d="M423.602441 746.060699c6.47054-6.297579 12.823107-7.017417 21.629121-2.784372 34.520213 16.582259 70.232157 19.645568 107.031855 9.116944 8.118169-2.323476 15.974396-5.475765 23.598677-9.22392 13.712907-6.73648 26.003134 0.8878 26.080116 16.13936 0.109975 22.574907-0.024994 45.142816 0.080982 67.709725 0.031993 7.464316-2.277486 13.322995-9.44387 16.608254-7.277358 3.333248-13.765895 1.961558-19.526595-3.264264-3.653176-3.313253-7.063407-6.897444-10.634601-10.304675-6.563519-6.259588-6.676494-6.25259-10.625603 1.603638-8.437097 16.80121-16.821205 33.623415-25.257302 50.423625-2.489438 4.953882-5.706713 9.196925-11.411426 10.775569-8.355115 2.315478-15.772442-1.070758-20.272427-9.867774-8.774021-17.15313-17.269104-34.453228-25.918153-51.669344-3.750154-7.469315-3.9891-7.479313-10.141712-1.514658-3.715162 3.602187-7.31435 7.326347-11.142486 10.800563-5.571743 5.060858-11.934308 6.269586-18.936728 3.207277-6.82746-2.984327-9.869774-8.483086-9.892769-15.685462-0.070984-23.506697-0.041991-47.018393-0.020995-70.532089 0.007998-4.679944 1.46467-8.785018 4.803916-11.538397z"
            fill="#000000"></path>
        </svg>
      </div>

      <el-popover placement="left" :close-delay="500" trigger="hover">
        <template #reference>
          <div>
            <i class="fa fa-cog iconRotate" style="color: var(--black)" aria-hidden="true"></i>
          </div>
        </template>
        <div class="my-setting">
          <div>
            <!-- 太阳按钮 -->
            <i v-if="isDark" class="el-icon-sunny iconRotate" @click="changeColor()"></i>
            <!-- 月亮按钮 -->
            <i v-else class="fa fa-moon-o" aria-hidden="true" @click="changeColor()"></i>
          </div>
        </div>
      </el-popover>
    </div>
    <div>
      <Sider />
    </div>
    <!-- 图片预览 -->
    <div id="outerImg">
      <div id="innerImg" style="position:absolute">
        <img id="bigImg" src="" />
      </div>
    </div>
  </div>
</template>



<style scoped>
.toolbar-content {
  width: 100%;
  height: 60px;
  color: var(--toolbarFont);
  background: white;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  user-select: none;
  transition: all 0.3s ease-in-out;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

#main-container {
  padding-top: 60px;
}

.toolbar-content.enter {
  background: var(--toolbarBackground);
  color: var(--toolbarFont);
  box-shadow: 0 1px 3px 0 rgba(0, 34, 77, 0.05);
}

.toolbar-content.hoverEnter {
  background: var(--translucent);
  box-shadow: 0 1px 3px 0 rgba(0, 34, 77, 0.05);
}

.toolbar-title {
  margin-left: 30px;
  cursor: pointer;
}

.toolbar-mobile-menu {
  font-size: 30px;
  margin-right: 15px;
  cursor: pointer;
}

.scroll-menu {
  margin: 0 25px 0 0;
  display: flex;
  justify-content: flex-end;
  padding: 0;
}

.scroll-menu li {
  list-style: none;
  margin: 0 12px;
  font-size: 17px;
  height: 60px;
  line-height: 60px;
  position: relative;
  cursor: pointer;
}

.scroll-menu li:hover .my-menu span {
  color: var(--themeBackground);
}

.scroll-menu li:hover .my-menu i {
  color: var(--themeBackground);
  animation: scale 1.5s ease-in-out infinite;
}

.scroll-menu li .my-menu:after {
  content: "";
  display: block;
  position: absolute;
  bottom: 0;
  height: 6px;
  background-color: var(--themeBackground);
  width: 100%;
  max-width: 0;
  transition: max-width 0.25s ease-in-out;
}

.scroll-menu li:hover .my-menu:after {
  max-width: 100%;
}

.sortMenu {
  margin-left: 44px;
  font-size: 17px;
  position: relative;
}

.sortMenu:after {
  top: 32px;
  width: 35px;
  left: 0;
  height: 2px;
  background: var(--themeBackground);
  content: "";
  border-radius: 1px;
  position: absolute;
}

.el-dropdown {
  font-size: unset;
  color: unset;
}

.el-popper[x-placement^=bottom] {
  margin-top: -8px;
}

.el-dropdown-menu {
  padding: 5px 0;
}

.el-dropdown-menu__item {
  font-size: unset;
}

.el-dropdown-menu__item:hover {
  background-color: var(--white);
  color: var(--themeBackground);
}

.toolButton {
  position: fixed;
  right: 3vh;
  bottom: 3vh;
  animation: slide-bottom 0.5s ease-in-out both;
  z-index: 100;
  cursor: pointer;
  font-size: 25px;
  width: 30px;
}

.my-setting {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
  cursor: pointer;
  font-size: 20px;
}

.my-setting i {
  padding: 5px;
}

.my-setting i:hover {
  color: var(--themeBackground);
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

.backTop {
  transition: all 0.3s ease-in;
  position: relative;
  top: 0;
  left: -13px;
}

.backTop:hover {
  top: -10px;
}

#outerImg {
  position: fixed;
  top: 0;
  left: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 10;
  width: 100%;
  height: 100%;
  display: none;
}

@media screen and (max-width: 400px) {
  .toolButton {
    right: 0.5vh;
  }
}
</style>
