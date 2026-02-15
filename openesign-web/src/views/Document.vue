<template>
  <div class="document-detail">
    <el-button @click="$router.back()" style="margin-bottom: 20px">
      <el-icon><ArrowLeft /></el-icon>
      返回
    </el-button>
    
    <el-row :gutter="20" v-loading="loading">
      <!-- Document Info -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>文档信息</span>
            </div>
          </template>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="标题">{{ document.title }}</el-descriptions-item>
            <el-descriptions-item label="文件名">{{ document.fileName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(document.documentStatus)">
                {{ getStatusText(document.documentStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(document.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="描述">
              {{ document.description || '-' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div style="margin-top: 20px">
            <el-button type="primary" @click="downloadDocument">
              <el-icon><Download /></el-icon>
              下载文档
            </el-button>
          </div>
        </el-card>
        
        <!-- Recipients -->
        <el-card style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>签署人</span>
            </div>
          </template>
          
          <el-table :data="document.recipients" v-if="document.recipients?.length">
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="recipientStatus" label="状态">
              <template #default="{ row }">
                <el-tag :type="getRecipientStatusType(row.recipientStatus)" size="small">
                  {{ getRecipientStatusText(row.recipientStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-else description="暂无签署人" />
        </el-card>
      </el-col>
      
      <!-- Document Preview -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>文档预览</span>
            </div>
          </template>
          
          <div class="pdf-preview">
            <el-empty description="PDF预览功能开发中" />
          </div>
        </el-card>
        
        <!-- Signature Fields -->
        <el-card style="margin-top: 20px" v-if="document.fields?.length">
          <template #header>
            <div class="card-header">
              <span>签名域</span>
            </div>
          </template>
          
          <el-table :data="document.fields">
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="page" label="页码" width="80" />
            <el-table-column prop="fieldStatus" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.fieldStatus === 'SIGNED' ? 'success' : 'warning'" size="small">
                  {{ row.fieldStatus === 'SIGNED' ? '已签署' : '待签署' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="signedAt" label="签署时间">
              <template #default="{ row }">
                {{ row.signedAt ? formatDate(row.signedAt) : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { ArrowLeft, Download } from '@element-plus/icons-vue'

const route = useRoute()

const loading = ref(false)
const document = ref({})

const loadDocument = async () => {
  loading.value = true
  try {
    const response = await api.documents.get(route.params.id)
    document.value = response.data
  } catch (error) {
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

const downloadDocument = async () => {
  try {
    const response = await api.documents.download(route.params.id)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = document.value.fileName || 'document.pdf'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const getStatusType = (status) => {
  const map = { 'DRAFT': 'info', 'PENDING': 'warning', 'COMPLETED': 'success', 'DECLINED': 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'DRAFT': '草稿', 'PENDING': '待签署', 'COMPLETED': '已完成', 'DECLINED': '已拒绝' }
  return map[status] || status
}

const getRecipientStatusType = (status) => {
  const map = { 'PENDING': 'info', 'SENT': 'warning', 'SIGNED': 'success', 'DECLINED': 'danger' }
  return map[status] || 'info'
}

const getRecipientStatusText = (status) => {
  const map = { 'PENDING': '待发送', 'SENT': '已发送', 'SIGNED': '已签署', 'DECLINED': '已拒绝' }
  return map[status] || status
}

const formatDate = (date) => {
  return date ? new Date(date).toLocaleString('zh-CN') : '-'
}

onMounted(() => {
  loadDocument()
})
</script>

<style scoped>
.document-detail {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.card-header {
  font-weight: bold;
}

.pdf-preview {
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
</style>
