<template>
  <div class="sign-document">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>签署文档</span>
        </div>
      </template>
      
      <div v-if="document.title">
        <h2>{{ document.title }}</h2>
        <p class="status">状态: <el-tag :type="getStatusType(document.documentStatus)">
          {{ getStatusText(document.documentStatus) }}
        </el-tag></p>
        
        <el-divider />
        
        <div v-if="pendingFields.length > 0">
          <h3>待签署签名域</h3>
          
          <div v-for="field in pendingFields" :key="field.id" class="signature-field">
            <div class="field-info">
              <span>类型: {{ field.type }}</span>
              <span>页码: {{ field.page }}</span>
            </div>
            
            <div class="signature-input">
              <el-input
                v-if="field.type === 'TEXT'"
                v-model="field.textValue"
                placeholder="输入文本"
              />
              
              <div v-else class="signature-draw">
                <canvas 
                  ref="canvasRef" 
                  width="400" 
                  height="150"
                  @mousedown="startDraw"
                  @mousemove="draw"
                  @mouseup="endDraw"
                  @mouseleave="endDraw"
                ></canvas>
                <div class="signature-tools">
                  <el-button size="small" @click="clearCanvas">清除</el-button>
                </div>
              </div>
              
              <el-button 
                type="primary" 
                style="margin-top: 10px"
                @click="signField(field)"
              >
                确认签署
              </el-button>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无待签署的签名域" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const route = useRoute()

const loading = ref(false)
const document = ref({})
const canvasRef = ref(null)
const isDrawing = ref(false)
const signatureData = ref('')

let ctx = null

const pendingFields = computed(() => {
  return (document.value.fields || []).filter(f => f.fieldStatus === 'PENDING')
})

const loadDocument = async () => {
  loading.value = true
  try {
    const response = await api.documents.get(route.params.id)
    document.value = response.data
    
    // Initialize canvas after DOM is ready
    setTimeout(initCanvas, 100)
  } catch (error) {
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

const initCanvas = () => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    ctx.strokeStyle = '#000'
    ctx.lineWidth = 2
    ctx.lineCap = 'round'
  }
}

const startDraw = (e) => {
  isDrawing.value = true
  ctx.beginPath()
  ctx.moveTo(e.offsetX, e.offsetY)
}

const draw = (e) => {
  if (!isDrawing.value) return
  ctx.lineTo(e.offsetX, e.offsetY)
  ctx.stroke()
}

const endDraw = () => {
  isDrawing.value = false
  signatureData.value = canvasRef.value.toDataURL('image/png')
}

const clearCanvas = () => {
  if (ctx && canvasRef.value) {
    ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height)
    signatureData.value = ''
  }
}

const signField = async (field) => {
  try {
    await api.documents.sign(field.id, signatureData.value, field.textValue)
    ElMessage.success('签署成功')
    loadDocument()
  } catch (error) {
    ElMessage.error('签署失败')
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

onMounted(() => {
  loadDocument()
})
</script>

<style scoped>
.sign-document {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}

.status {
  margin-top: 10px;
  color: #606266;
}

.signature-field {
  margin: 20px 0;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.field-info {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  color: #606266;
}

.signature-draw canvas {
  border: 1px solid #dcdfe6;
  background: white;
  cursor: crosshair;
}

.signature-tools {
  margin-top: 10px;
  text-align: right;
}
</style>
