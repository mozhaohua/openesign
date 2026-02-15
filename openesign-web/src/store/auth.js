import { defineStore } from 'pinia'
import api from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: null
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token
  },
  
  actions: {
    async login(email, password) {
      const response = await api.auth.login(email, password)
      this.token = response.data.token
      this.user = response.data.user
      localStorage.setItem('token', this.token)
      api.setToken(this.token)
    },
    
    async register(email, password, name, company) {
      const response = await api.auth.register(email, password, name, company)
      this.token = response.data.token
      this.user = response.data.user
      localStorage.setItem('token', this.token)
      api.setToken(this.token)
    },
    
    async fetchCurrentUser() {
      if (!this.token) return
      try {
        api.setToken(this.token)
        const response = await api.auth.getCurrentUser()
        this.user = response.data
      } catch (error) {
        this.logout()
      }
    },
    
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      api.setToken(null)
    }
  }
})
