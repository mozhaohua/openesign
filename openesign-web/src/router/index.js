import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'documents/:id',
        name: 'Document',
        component: () => import('@/views/Document.vue')
      },
      {
        path: 'documents/:id/sign',
        name: 'SignDocument',
        component: () => import('@/views/SignDocument.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue')
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/views/Templates.vue')
      }
    ],
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
