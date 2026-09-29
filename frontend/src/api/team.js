import request from './request'

// 团队接口
export const listTeams = () => request.get('/teams')
export const getTeam = (teamId) => request.get(`/teams/${teamId}`)
export const createTeam = (data) => request.post('/teams', data)
export const addMember = (teamId, data) => request.post(`/teams/${teamId}/members`, data)
export const removeMember = (teamId, userId) => request.delete(`/teams/${teamId}/members/${userId}`)
export const updateRole = (teamId, userId, role) => request.put(`/teams/${teamId}/members/${userId}/role`, { role })
