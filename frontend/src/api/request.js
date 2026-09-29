import axios from 'axios'
import { ElMessage } from 'element-plus'

// axios 实例：所有请求走 /api，由 Vite 代理转发到后端
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器：自动带上 access token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ---- 无感刷新：access token 过期时用 refresh token 换新，再重放原请求 ----
let isRefreshing = false
let pendingQueue = []

function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
}

function toLogin() {
  clearAuth()
  ElMessage.error('登录已过期，请重新登录')
  window.location.href = '#/login'
}

// 用 refresh token 换新 token，成功返回 true
async function refreshToken() {
  const old = localStorage.getItem('refreshToken')
  if (!old) return false
  // 第三个参数标记这是刷新请求本身，避免 401 时再次触发刷新导致死循环
  const data = await request.post('/auth/refresh', { refreshToken: old }, { _isRefresh: true })
  localStorage.setItem('token', data.accessToken)
  localStorage.setItem('refreshToken', data.refreshToken)
  return true
}

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 刷新请求失败不单独弹错，由统一登出逻辑提示“登录已过期”
    if (!response.config._isRefresh) {
      ElMessage.error(res.message || '请求失败')
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  async (error) => {
    const { response, config } = error
    const is401 = response && response.status === 401
    // 401 且不是刷新请求本身、且没重试过 -> 尝试刷新后重放
    if (is401 && !config._retried && !config._isRefresh) {
      config._retried = true
      if (isRefreshing) {
        // 已有刷新在进行，排队等刷新结果
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject, config })
        })
      }
      isRefreshing = true
      try {
        const ok = await refreshToken()
        if (ok) {
          // 刷新成功：重放排队请求 + 当前请求
          pendingQueue.forEach(({ resolve, config: c }) => resolve(request(c)))
          pendingQueue = []
          return request(config)
        }
      } catch (e) {
        // 刷新失败（refresh token 也失效）
      } finally {
        isRefreshing = false
      }
      pendingQueue.forEach(({ reject }) => reject(error))
      pendingQueue = []
      toLogin()
      return Promise.reject(error)
    }
    if (is401) {
      toLogin()
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

export default request
