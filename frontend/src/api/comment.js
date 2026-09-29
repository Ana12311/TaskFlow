import request from './request'

// 评论接口
export const createComment = (taskId, content) => request.post(`/tasks/${taskId}/comments`, { content })
export const listComments = (taskId, pageNum = 1, pageSize = 10) =>
  request.get(`/tasks/${taskId}/comments`, { params: { pageNum, pageSize } })
export const deleteComment = (commentId) => request.delete(`/comments/${commentId}`)
