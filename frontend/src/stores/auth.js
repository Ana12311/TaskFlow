import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as authApi from '../api/auth'
import * as userApi from '../api/user'

// 登录态仓库：access token + refresh token + 当前用户，持久化到 localStorage
export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  function setAuth(newToken, newRefreshToken, newUser) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    user.value = newUser
    localStorage.setItem('token', newToken)
    localStorage.setItem('refreshToken', newRefreshToken)
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  async function login(username, password) {
    const data = await authApi.login(username, password)
    setAuth(data.accessToken, data.refreshToken, data.user)
  }

  async function register(payload) {
    await authApi.register(payload)
  }

  // 登出：先清本地（立即生效），再通知后端失效 token（后端调用失败也不阻塞登出）
  async function logout() {
    // 从 localStorage 取最新值：无感刷新后 request.js 会更新 localStorage，而 store 里的 ref 可能是旧的
    const rt = localStorage.getItem('refreshToken')
    token.value = ''
    refreshToken.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    try {
      await authApi.logout(rt)
    } catch (e) {
      // 忽略：后端失效失败不影响本地登出
    }
  }

  // 刷新当前用户信息（改资料后同步本地缓存）
  async function fetchMe() {
    const data = await userApi.me()
    user.value = data
    localStorage.setItem('user', JSON.stringify(data))
  }

  return { token, refreshToken, user, login, register, logout, fetchMe }
})
