import request from './request'

// 当前用户接口
export const me = () => request.get('/users/me')
export const updateProfile = (data) => request.put('/users/me', data)
