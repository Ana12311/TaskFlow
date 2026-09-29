// 字典：状态/优先级/角色 -> 中文标签 + 标签颜色

export const STATUS_MAP = {
  TODO: { label: '待办', type: 'info' },
  IN_PROGRESS: { label: '进行中', type: 'warning' },
  DONE: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' },
}

export const PRIORITY_MAP = {
  LOW: { label: '低', type: 'info' },
  MEDIUM: { label: '中', type: '' },
  HIGH: { label: '高', type: 'warning' },
  URGENT: { label: '紧急', type: 'danger' },
}

export const ROLE_LABELS = {
  OWNER: '拥有者',
  ADMIN: '管理员',
  MEMBER: '成员',
}

export const OPERATION_LABELS = {
  CREATE: '创建任务',
  ASSIGNEE_CHANGED: '修改负责人',
  STATUS_CHANGED: '修改状态',
  PRIORITY_CHANGED: '修改优先级',
  DELETE: '删除任务',
}

export const NOTIFICATION_TYPE_LABELS = {
  TASK_ASSIGNED: '任务分配给你',
  TASK_COMMENTED: '任务有新评论',
  TASK_STATUS_CHANGED: '任务状态变更',
  TEAM_INVITED: '加入团队',
  PROJECT_INVITED: '加入项目',
}
