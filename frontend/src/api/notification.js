import request from './request'

// 通知接口
export const listNotifications = (pageNum = 1, pageSize = 10) =>
  request.get('/notifications', { params: { pageNum, pageSize } })
export const getUnreadCount = () => request.get('/notifications/unread-count')
export const markRead = (id) => request.put(`/notifications/${id}/read`)
export const markAllRead = () => request.put('/notifications/read-all')
