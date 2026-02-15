<template>
  <div class="dashboard">
    <div class="header">
      <h1>我的文档</h1>
      <el-button type="primary" @click="showUploadDialog = true">
        <el-icon><Upload /></el-icon>
        上传文档
      </el-button>
    </div>
    
    <!-- Document List -->
    <el-table :data="documents" v-loading="loading" style="width: 100%">
      <el-table-column prop="title" label="文档名称" min-width="200" />
      <el-table-column prop="fileName" label="文件名" min-width="150" />
      <el-table-column prop="documentStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.documentStatus)">
            {{ getStatusText(row.documentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewDocument(row.id)">
            查看
          </el-button>
          <el-button 
            v-if="row.documentStatus === 'DRAFT'" 
            type="success" 
            size="small" 
            @click="sendDocument(row.id)"
          >
            发送
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- Upload Dialog -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        accept=".pdf"
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF 文件，最大 50MB
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { Upload } from '@element-plus/icons-vue'

const router = useRouter()

const documents = ref([])
const loading = ref(false)
const showUploadDialog = ref(false)
const uploadRef = ref()
const fileList = ref([])
const uploading = ref(false)
const uploadFile = ref(null)

const loadDocuments = async () => {
  loading.value = true
  try {
    const response = await api.documents.list()
    documents.value = response.data
  } catch (error) {
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

const handleUpload = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  uploading.value = true
  try {
    await api.documents.upload(uploadFile.value)
    ElMessage.success('上传成功')
    showUploadDialog.value = false
    fileList.value = []
    uploadFile.value = null
    loadDocuments()
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const viewDocument = (id) => {
  router.push(`/documents/${id}`)
}

const sendDocument = async (id) => {
  try {
    await api.documents.send(id)
    ElMessage.success('发送成功')
    loadDocuments()
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const getStatusType = (status) => {
  const map = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'COMPLETED': 'success',
    'DECLINED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'DRAFT': '草稿',
    'PENDING': '待签署',
    'COMPLETED': '已完成',
    'DECLINED': '已拒绝'
  }
  return map[status] || status
}

const formatDate = (date) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.dashboard {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 20px;
  margin: 0;
}
</style>
