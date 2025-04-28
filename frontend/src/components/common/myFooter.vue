<template>
  <div class="myFooter-wrap" v-show="showFooter">
    <div class="myFooter">
      <div class="footer-title">{{ footerText }}</div>
      <div class="icp">
        本网站由
        <a href="https://aircraft-carrier.github.io/" target="_blank">AIRCRAFT</a> 
        强力支持 
        <a href="https://aircraft-carrier.github.io/" target="_blank">{{ beian }}</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  showFooter: {
    type: Boolean,
    default: true
  }
});

const footerText = ref('This is a demo of a heart segmentation');
const beian = ref('1111');
</script>

<style scoped>
.myFooter-wrap {
  user-select: none;
  animation: hideToShow 2s;
}

.myFooter {
  border-radius: 1.5rem 1.5rem 0 0;
  background: var(--gradientBG);
  text-align: center;
  color: var(--white);
  background-size: 300% 300%;
  animation: gradientBG 10s ease infinite;
}

.footer-title {
  padding-top: 10px;
  font-size: 16px;
}

.icp, .icp a {
  color: var(--maxGreyFont);
  font-size: 13px;
}

.icp {
  padding-top: 10px;
  padding-bottom: 10px;
}

.icp a {
  text-decoration: none;
  transition: all 0.3s;
}

.icp a:hover {
  color: var(--themeBackground);
}
</style>
