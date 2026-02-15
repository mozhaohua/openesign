<template>
  <div class="register-container">
    <div class="register-box">
      <div class="logo">
        <h1>OpenESign</h1>
        <p>创建您的账号</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleRegister">
        <el-form-item prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="姓名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="company">
          <el-input 
            v-model="form.company" 
            placeholder="公司（可选）"
            prefix-icon="OfficeBuilding"
          />
        </el-form-item>
        
        <el-button type="primary" :loading="loading" @click="handleRegister" class="register-btn">
          注册
        </el-button>
      </el-form>
      
      <div class="footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  email: '',
  password: '',
  name: '',
  company: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await authStore.register(form.email, form.password, form.name, form.company)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 400px;
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo h1 {
  color: #333;
  font-size: 28px;
  margin-bottom: 8px;
}

.logo p {
  color: #999;
  font-size: 14px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-top: 10px;
}

.footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}

.footer a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}
</style>
