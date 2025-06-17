<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>修改密码</span>
      </div>
    </template>
    <el-row>
      <el-col :span="20">
        <el-form :model="passwordForm" :rules="rules" ref="passwordFormRef" label-width="100px" size="large">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password style="width: 100%;"></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password style="width: 100%;"></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password style="width: 100%;"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updatePassword">提交修改</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userPasswordUpdateService } from '@/api/user.js'

const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (passwordForm.value.confirmPassword !== '') {
      passwordFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// 修改密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const result = await userPasswordUpdateService({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword,
          confirmPassword: passwordForm.value.confirmPassword
        })
        ElMessage.success(result.msg ? result.msg : '密码修改成功')
        // 清空表单
        passwordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
      }
    }
  })
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.header {
  font-size: 16px;
  font-weight: bold;
}
</style>
