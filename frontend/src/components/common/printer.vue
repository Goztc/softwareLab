<template>
  <div>
    <slot name="paper" :content="content"></slot>
  </div>
</template>
<script setup>  
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';  

const props = defineProps({  
  printerInfo: {  
    type: String,  
    default: ""  
  },  
  duration: {  
    type: Number,  
    default: 100  
  },  
  delay: {  
    type: Number,  
    default: 3000  
  },  
  working: {  
    type: Boolean,  
    default: true  
  },  
  once: {  
    type: Boolean,  
    default: false  
  }  
});  

const content = ref("");  
const cursor = ref(0);  
let timer = null;  
let timeout = null;  
const print = ref(true);  

const start = (work) => {  
  timeout = setTimeout(() => {  
    timer = setInterval(() => {  
      work();  
      if (cursor.value === 0 || (cursor.value === props.printerInfo.length && !props.once)) {  
        clearInterval(timer);  
        start(work);  
      } else if (cursor.value === props.printerInfo.length && props.once) {  
        clearInterval(timer);  
      }  
    }, props.duration);  
  }, props.delay);  
};  

const work = () => {  
  let currentCursor = cursor.value;  
  currentCursor += print.value ? 1 : -1;  
  if (print.value) {  
    if (currentCursor === props.printerInfo.length + 1) {  
      currentCursor -= 2;  
      print.value = !print.value;  
    }  
  } else {  
    if (currentCursor === -1) {  
      currentCursor += 2;  
      print.value = !print.value;  
    }  
  }  
  cursor.value = currentCursor;  
};  

const toBegin = () => {  
  cursor.value = 0;  
  if (timeout !== null) {  
    clearTimeout(timeout);  
    if (timer !== null) {  
      clearInterval(timer);  
    }  
  }  
  if (props.working) {  
    start(work);  
  } else {  
    content.value = props.printerInfo;  
  }  
};  

watch(() => props.working, toBegin);  
watch(() => props.printerInfo, toBegin);  

watch(cursor, () => {  
  if (typeof props.printerInfo === 'string') {  
    content.value = props.printerInfo.slice(0, cursor.value);  
  } else {  
    console.error('printerInfo is not a string:', props.printerInfo);  
  }  
});  

onMounted(() => {  
  if (props.working) {  
    start(work);  
  } else {  
    content.value = props.printerInfo;  
  }  
});  

onBeforeUnmount(() => {  
  clearInterval(timer);  
});  
</script>