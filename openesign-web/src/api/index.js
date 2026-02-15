import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.setToken = (token) => {
  if (token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`
  } else {
    delete api.defaults.headers.common['Authorization']
  }
}

// Initialize token from localStorage
const token = localStorage.getItem('token')
if (token) {
  api.setToken(token)
}

export default {
  auth: {
    login(email, password) {
      return api.post('/auth/login', { email, password })
    },
    register(email, password, name, company) {
      return api.post('/auth/register', { email, password, name, company })
    },
    getCurrentUser() {
      return api.get('/auth/me')
    }
  },
  
  documents: {
    list() {
      return api.get('/documents')
    },
    get(id) {
      return api.get(`/documents/${id}`)
    },
    upload(file) {
      const formData = new FormData()
      formData.append('file', file)
      return api.post('/documents/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    create(data) {
      return api.post('/documents', data)
    },
    send(id) {
      return api.post(`/documents/${id}/send`)
    },
    sign(fieldId, signatureData, textValue) {
      return api.post('/documents/sign', { fieldId, signatureData, textValue })
    },
    download(id) {
      return api.get(`/documents/${id}/download`, { responseType: 'blob' })
    }
  }
}
