import request from './request'

// 任务接口
export const createTask = (projectId, data) => request.post(`/projects/${projectId}/tasks`, data)
export const listTasks = (projectId, pageNum = 1, pageSize = 10) =>
  request.get(`/projects/${projectId}/tasks`, { params: { pageNum, pageSize } })
export const getStats = (projectId) => request.get(`/projects/${projectId}/stats`)
export const getTask = (taskId) => request.get(`/tasks/${taskId}`)
export const updateTask = (taskId, data) => request.put(`/tasks/${taskId}`, data)
export const deleteTask = (taskId) => request.delete(`/tasks/${taskId}`)
export const assignTask = (taskId, assigneeId) => request.put(`/tasks/${taskId}/assignee`, { assigneeId })
export const updateStatus = (taskId, status) => request.put(`/tasks/${taskId}/status`, { status })
export const updatePriority = (taskId, priority) => request.put(`/tasks/${taskId}/priority`, { priority })
export const listLogs = (taskId, pageNum = 1, pageSize = 10) =>
  request.get(`/tasks/${taskId}/logs`, { params: { pageNum, pageSize } })
