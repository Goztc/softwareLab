<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>更换头像</span>
      </div>
    </template>
    <el-row>
      <el-col :span="12">
        <el-upload
          ref="uploadRef"
          class="avatar-uploader"
          :show-file-list="false"
          :auto-upload="true"
          action="/api/upload"
          name="file"
          :headers="{'Authorization': tokenStore.token}"
          :on-success="uploadSuccess"
        >
          <img v-if="imgUrl" :src="imgUrl" class="avatar" />
          <img v-else :src="avatar" class="avatar" />

        </el-upload>
        <br />
        <div class="button-container">
          <el-button
            type="primary"
            :icon="Plus"
            size="large"
            @click="uploadRef.$el.querySelector('input').click()"
          >
            选择图片
          </el-button>
          <el-button
            type="success"
            :icon="Upload"
            size="large"
            @click="updateAvatar"
          >
            上传头像
          </el-button>
        </div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref } from 'vue';
import { Plus, Upload } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

import avatar from '@/assets/default.png';
import { useTokenStore } from '@/stores/token.js';
import useUserInfoStore from '@/stores/userInfo.js';
import { userAvatarUpdateService } from '@/api/user.js';

const uploadRef = ref();
const tokenStore = useTokenStore();
const userInfoStore = useUserInfoStore();

// 用户头像地址
const imgUrl = ref(userInfoStore.info.userPic);

// 图片上传成功的回调函数
const uploadSuccess = (result) => {
  imgUrl.value = result.data;
};

// 头像修改
const updateAvatar = async () => {
  // 调用接口
  const result = await userAvatarUpdateService(imgUrl.value);

  ElMessage.success(result.msg ? result.msg : '修改成功');

  // 修改 pinia 中的数据
  userInfoStore.info.userPic = imgUrl.value;
};
</script>

<style lang="scss" scoped>
.avatar-uploader {
  :deep() {
    .avatar {
      width: 350px;
      height: 278px;
      display: block;
    }

    .el-upload {
      border: 1px dashed var(--el-border-color);
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
    }

    .el-upload:hover {
      border-color: var(--el-color-primary);
    }

    .el-icon.avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 278px;
      height: 278px;
      text-align: center;
    }
  }
}
.header {
    font-size: 16px;
    font-weight: bold;
}

.button-container {
  display: flex;
  justify-content: center; /* 水平居中对齐 */
  gap: 20px; /* 按钮之间的间距 */
  margin-top: 20px; /* 距离图片的间距 */
}
</style>
