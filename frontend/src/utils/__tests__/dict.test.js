import { describe, it, expect } from 'vitest'
import {
  STATUS_MAP,
  PRIORITY_MAP,
  ROLE_LABELS,
  OPERATION_LABELS,
  NOTIFICATION_TYPE_LABELS,
} from '../dict'

describe('dict 字典', () => {
  it('状态映射覆盖四种状态', () => {
    expect(STATUS_MAP.TODO.label).toBe('待办')
    expect(STATUS_MAP.IN_PROGRESS.label).toBe('进行中')
    expect(STATUS_MAP.DONE.type).toBe('success')
    expect(STATUS_MAP.CANCELLED.type).toBe('danger')
  })

  it('优先级映射覆盖四种优先级', () => {
    expect(PRIORITY_MAP.URGENT.label).toBe('紧急')
    expect(PRIORITY_MAP.URGENT.type).toBe('danger')
    expect(PRIORITY_MAP.LOW.label).toBe('低')
  })

  it('角色标签齐全', () => {
    expect(ROLE_LABELS.OWNER).toBe('拥有者')
    expect(ROLE_LABELS.ADMIN).toBe('管理员')
    expect(ROLE_LABELS.MEMBER).toBe('成员')
  })

  it('操作日志标签齐全', () => {
    expect(OPERATION_LABELS.CREATE).toBe('创建任务')
    expect(OPERATION_LABELS.STATUS_CHANGED).toBe('修改状态')
  })

  it('通知类型标签齐全', () => {
    expect(NOTIFICATION_TYPE_LABELS.TASK_ASSIGNED).toBe('任务分配给你')
    expect(NOTIFICATION_TYPE_LABELS.PROJECT_INVITED).toBe('加入项目')
  })
})
