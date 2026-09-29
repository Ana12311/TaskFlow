import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/teams' },
      { path: 'teams', component: () => import('../views/Teams.vue') },
      { path: 'teams/:teamId', component: () => import('../views/TeamDetail.vue') },
      { path: 'projects/:projectId', component: () => import('../views/ProjectDetail.vue') },
      { path: 'tasks/:taskId', component: () => import('../views/TaskDetail.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 路由守卫：未登录访问受保护页 -> 跳登录；已登录访问登录页 -> 跳首页
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  if ((to.path === '/login' || to.path === '/register') && token) {
    return '/teams'
  }
})

export default router
