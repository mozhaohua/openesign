<template>
  <div class="pdf-viewer" ref="containerRef">
    <div class="pdf-toolbar">
      <el-button-group>
        <el-button :icon="ZoomOut" @click="zoomOut" :disabled="scale <= 0.5">-</el-button>
        <el-button :icon="ZoomIn" @click="zoomIn" :disabled="scale >= 3">+</el-button>
      </el-button-group>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <el-button-group>
        <el-button :icon="ArrowLeft" @click="prevPage" :disabled="currentPage <= 1">Prev</el-button>
        <el-button :icon="ArrowRight" @click="nextPage" :disabled="currentPage >= totalPages">Next</el-button>
      </el-button-group>
      <el-select v-model="pageDisplay" @change="goToPage" placeholder="Go to page" style="width: 100px">
        <el-option v-for="p in totalPages" :key="p" :label="`Page ${p}`" :value="p" />
      </el-select>
    </div>
    <div class="pdf-content" ref="contentRef">
      <div class="pdf-page-wrapper" ref="pageWrapperRef">
        <canvas ref="canvasRef"></canvas>
        <div
          v-for="field in visibleFields"
          :key="field.id"
          class="signature-field"
          :class="{ 'is-signed': field.isSigned, 'is-active': activeFieldId === field.id }"
          :style="getFieldStyle(field)"
          @click="handleFieldClick(field)"
        >
          <span v-if="!field.isSigned" class="field-placeholder">{{ field.label || 'Sign Here' }}</span>
          <img v-else-if="field.signatureData" :src="field.signatureData" class="field-signature" />
          <span v-else class="field-signed-text">{{ field.textValue }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import * as pdfjsLib from 'pdfjs-dist'
import { ZoomIn, ZoomOut, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/4.0.379/pdf.worker.min.js'

const props = defineProps({
  url: String,
  data: Blob,
  fields: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['loaded', 'error', 'field-click'])

const containerRef = ref(null)
const contentRef = ref(null)
const canvasRef = ref(null)
const pageWrapperRef = ref(null)

const currentPage = ref(1)
const totalPages = ref(0)
const scale = ref(1.0)
const pageDisplay = ref(1)
const pdfDoc = ref(null)
const activeFieldId = ref(null)

const visibleFields = ref([])

const loadPdf = async () => {
  try {
    let loadingTask
    if (props.data) {
      loadingTask = pdfjsLib.getDocument({ data: props.data })
    } else if (props.url) {
      loadingTask = pdfjsLib.getDocument(props.url)
    } else {
      return
    }

    pdfDoc.value = await loadingTask.promise
    totalPages.value = pdfDoc.value.numPages
    emit('loaded', { numPages: totalPages.value })
    await renderPage(currentPage.value)
  } catch (error) {
    console.error('Error loading PDF:', error)
    emit('error', error)
  }
}

const renderPage = async (pageNum) => {
  if (!pdfDoc.value) return

  const page = await pdfDoc.value.getPage(pageNum)
  const viewport = page.getViewport({ scale: scale.value })

  const canvas = canvasRef.value
  const context = canvas.getContext('2d')

  canvas.height = viewport.height
  canvas.width = viewport.width

  const renderContext = {
    canvasContext: context,
    viewport: viewport
  }

  await page.render(renderContext).promise
  currentPage.value = pageNum
  pageDisplay.value = pageNum

  updateVisibleFields()
}

const updateVisibleFields = () => {
  visibleFields.value = props.fields.filter(f => f.page === currentPage.value)
}

const getFieldStyle = (field) => {
  return {
    left: `${field.x * scale.value}px`,
    top: `${field.y * scale.value}px`,
    width: `${field.width * scale.value}px`,
    height: `${field.height * scale.value}px`
  }
}

const handleFieldClick = (field) => {
  if (!field.isSigned) {
    activeFieldId.value = field.id
    emit('field-click', field)
  }
}

const setFields = (fields) => {
  updateVisibleFields()
}

const prevPage = () => {
  if (currentPage.value > 1) {
    renderPage(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    renderPage(currentPage.value + 1)
  }
}

const goToPage = (page) => {
  renderPage(page)
}

const zoomIn = () => {
  if (scale.value < 3) {
    scale.value += 0.25
    renderPage(currentPage.value)
  }
}

const zoomOut = () => {
  if (scale.value > 0.5) {
    scale.value -= 0.25
    renderPage(currentPage.value)
  }
}

const setScale = (newScale) => {
  scale.value = newScale
  renderPage(currentPage.value)
}

watch(() => props.url, () => {
  loadPdf()
})

watch(() => props.data, () => {
  loadPdf()
})

watch(() => props.fields, () => {
  updateVisibleFields()
}, { deep: true })

onMounted(() => {
  loadPdf()
})

defineExpose({
  currentPage,
  totalPages,
  scale,
  setScale,
  zoomIn,
  zoomOut,
  prevPage,
  nextPage,
  goToPage,
  renderPage,
  setFields
})
</script>

<style scoped>
.pdf-viewer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #525659;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #fff;
  border-bottom: 1px solid #ddd;
}

.page-info {
  margin: 0 15px;
  font-weight: 500;
}

.pdf-content {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  padding: 20px;
}

.pdf-page-wrapper {
  position: relative;
  display: inline-block;
}

.pdf-page-wrapper canvas {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  background: white;
}

.signature-field {
  position: absolute;
  border: 2px dashed #409eff;
  background: rgba(64, 158, 255, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #409eff;
  transition: all 0.2s;
}

.signature-field:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: #66b1ff;
}

.signature-field.is-signed {
  border-style: solid;
  border-color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
  cursor: default;
}

.signature-field.is-active {
  border-color: #f56c6c;
  background: rgba(245, 108, 108, 0.2);
  box-shadow: 0 0 8px rgba(245, 108, 108, 0.5);
}

.field-placeholder {
  user-select: none;
}

.field-signature {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.field-signed-text {
  font-family: cursive;
  font-size: 16px;
  color: #67c23a;
}
</style>
