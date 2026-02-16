<template>
  <div class="templates-container">
    <div class="page-header">
      <h2>签名模板管理</h2>
      <el-button type="primary" @click="openCreateDialog">创建模板</el-button>
    </div>

    <el-table :data="templates" v-loading="loading" style="width: 100%">
      <el-table-column prop="name" label="模板名称" min-width="150" />
      <el-table-column label="预览" width="180">
        <template #default="{ row }">
          <img v-if="row.signatureData" :src="row.signatureData" class="template-preview" />
          <span v-else class="type-signature" :style="{ fontFamily: 'cursive' }">{{ row.signatureData }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="signatureType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.signatureType === 'DRAW' ? 'primary' : row.signatureType === 'TYPE' ? 'success' : 'warning'" size="small">
            {{ row.signatureType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="primary" :disabled="row.isDefault" @click="setDefault(row.id)">设为默认</el-button>
          <el-button size="small" type="danger" @click="deleteTemplate(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模板' : '创建模板'" width="500px" @close="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="form.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="签名类型" required>
          <el-radio-group v-model="form.signatureType">
            <el-radio label="DRAW">手写签名</el-radio>
            <el-radio label="TYPE">输入文字</el-radio>
            <el-radio label="UPLOAD">上传图片</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.signatureType === 'DRAW'" label="手写签名">
          <canvas
            ref="drawCanvas"
            class="draw-canvas"
            @mousedown="startDraw"
            @mousemove="draw"
            @mouseup="stopDraw"
            @mouseleave="stopDraw"
          ></canvas>
          <div class="canvas-actions">
            <el-button size="small" @click="clearDraw">清除</el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="form.signatureType === 'TYPE'" label="输入文字">
          <el-input v-model="form.typeText" placeholder="输入您的签名" @input="updateTypePreview" />
          <div class="type-preview" :style="{ fontFamily: 'cursive' }">{{ form.typePreview }}</div>
        </el-form-item>
        <el-form-item v-if="form.signatureType === 'UPLOAD'" label="上传图片">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            @change="handleUpload"
          >
            <el-button>选择图片</el-button>
          </el-upload>
          <img v-if="form.uploadData" :src="form.uploadData" class="upload-preview" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-checkbox v-model="form.isDefault">默认模板</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :disabled="!canSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const templates = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const drawCanvas = ref(null)
let isDrawing = false

const form = reactive({
  name: '',
  signatureType: 'DRAW',
  typeText: '',
  typePreview: '',
  uploadData: '',
  isDefault: false
})

const canSubmit = computed(() => {
  if (!form.name) return false
  if (form.signatureType === 'DRAW' && !form.uploadData) return false
  if (form.signatureType === 'TYPE' && !form.typeText) return false
  if (form.signatureType === 'UPLOAD' && !form.uploadData) return false
  return true
})

const loadTemplates = async () => {
  loading.value = true
  try {
    const { data } = await api.signatureTemplates.list()
    templates.value = data
  } catch (error) {
    ElMessage.error('加载模板失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
  nextTick(() => initCanvas())
}

const openEditDialog = (template) => {
  isEdit.value = true
  editId.value = template.id
  form.name = template.name
  form.signatureType = template.signatureType
  form.isDefault = template.isDefault

  if (template.signatureType === 'DRAW' || template.signatureType === 'UPLOAD') {
    form.uploadData = template.signatureData
  } else if (template.signatureType === 'TYPE') {
    form.typeText = template.signatureData
    form.typePreview = template.signatureData
  }

  dialogVisible.value = true
  nextTick(() => {
    if (template.signatureType === 'DRAW' && template.signatureData) {
      initCanvas()
      loadSignatureToCanvas(template.signatureData)
    }
  })
}

const resetForm = () => {
  form.name = ''
  form.signatureType = 'DRAW'
  form.typeText = ''
  form.typePreview = ''
  form.uploadData = ''
  form.isDefault = false
}

const initCanvas = () => {
  if (!drawCanvas.value) return
  const canvas = drawCanvas.value
  canvas.width = 400
  canvas.height = 150
  const ctx = canvas.getContext('2d')
  ctx.strokeStyle = '#000'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
}

const loadSignatureToCanvas = (dataUrl) => {
  if (!drawCanvas.value || !dataUrl) return
  const canvas = drawCanvas.value
  const ctx = canvas.getContext('2d')
  const img = new Image()
  img.onload = () => {
    ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
  }
  img.src = dataUrl
}

const startDraw = (event) => {
  isDrawing = true
  const canvas = drawCanvas.value
  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()
  ctx.beginPath()
  ctx.moveTo(event.clientX - rect.left, event.clientY - rect.top)
}

const draw = (event) => {
  if (!isDrawing) return
  const canvas = drawCanvas.value
  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()
  ctx.lineTo(event.clientX - rect.left, event.clientY - rect.top)
  ctx.stroke()
}

const stopDraw = () => {
  if (isDrawing) {
    isDrawing = false
    form.uploadData = drawCanvas.value.toDataURL('image/png')
  }
}

const clearDraw = () => {
  if (!drawCanvas.value) return
  const canvas = drawCanvas.value
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  form.uploadData = ''
}

const updateTypePreview = () => {
  form.typePreview = form.typeText
}

const handleUpload = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.uploadData = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const submitForm = async () => {
  try {
    let signatureData = ''
    if (form.signatureType === 'DRAW' || form.signatureType === 'UPLOAD') {
      signatureData = form.uploadData
    } else if (form.signatureType === 'TYPE') {
      signatureData = form.typeText
    }

    const payload = {
      name: form.name,
      signatureData,
      signatureType: form.signatureType,
      isDefault: form.isDefault
    }

    if (isEdit.value) {
      await api.signatureTemplates.update(editId.value, payload)
      ElMessage.success('模板更新成功')
    } else {
      await api.signatureTemplates.create(payload)
      ElMessage.success('模板创建成功')
    }

    dialogVisible.value = false
    loadTemplates()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新模板失败' : '创建模板失败')
  }
}

const setDefault = async (id) => {
  try {
    await api.signatureTemplates.setDefault(id)
    ElMessage.success('默认模板已设置')
    loadTemplates()
  } catch (error) {
    ElMessage.error('设置默认模板失败')
  }
}

const deleteTemplate = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '确认删除', { type: 'warning' })
    await api.signatureTemplates.delete(id)
    ElMessage.success('模板已删除')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除模板失败')
    }
  }
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.templates-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.template-preview {
  max-width: 150px;
  max-height: 50px;
}

.type-signature {
  font-size: 20px;
  color: #409eff;
}

.draw-canvas {
  width: 100%;
  height: 150px;
  border: 1px solid #ddd;
  cursor: crosshair;
  background: #fff;
}

.canvas-actions {
  margin-top: 10px;
}

.type-preview {
  margin-top: 10px;
  font-size: 24px;
  text-align: center;
  color: #409eff;
}

.upload-preview {
  max-width: 100%;
  max-height: 150px;
  margin-top: 10px;
}
</style>
