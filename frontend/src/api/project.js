import request from './request'

// 项目接口
export const createProject = (teamId, data) => request.post(`/teams/${teamId}/projects`, data)
export const listTeamProjects = (teamId) => request.get(`/teams/${teamId}/projects`)
export const getProject = (projectId) => request.get(`/projects/${projectId}`)
export const addMember = (projectId, data) => request.post(`/projects/${projectId}/members`, data)
export const removeMember = (projectId, userId) => request.delete(`/projects/${projectId}/members/${userId}`)
export const deleteProject = (projectId) => request.delete(`/projects/${projectId}`)
