import request from './request'

// 认证接口
export const register = (data) => request.post('/auth/register', data)
export const login = (username, password) => request.post('/auth/login', { username, password })
// 用 refresh token 换新 token
export const refresh = (refreshToken) => request.post('/auth/refresh', { refreshToken })
// 登出（服务端失效 refresh token + 拉黑 access token）
export const logout = (refreshToken) => request.post('/auth/logout', { refreshToken })
