import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

import request from '../request'
import { listNotifications, getUnreadCount, markRead, markAllRead } from '../notification'

describe('notification api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listNotifications 默认带分页参数', () => {
    listNotifications()
    expect(request.get).toHaveBeenCalledWith('/notifications', { params: { pageNum: 1, pageSize: 10 } })
  })

  it('listNotifications 传自定义分页', () => {
    listNotifications(2, 50)
    expect(request.get).toHaveBeenCalledWith('/notifications', { params: { pageNum: 2, pageSize: 50 } })
  })

  it('getUnreadCount 请求路径正确', () => {
    getUnreadCount()
    expect(request.get).toHaveBeenCalledWith('/notifications/unread-count')
  })

  it('markRead 拼对单条已读路径', () => {
    markRead(7)
    expect(request.put).toHaveBeenCalledWith('/notifications/7/read')
  })

  it('markAllRead 请求全部已读', () => {
    markAllRead()
    expect(request.put).toHaveBeenCalledWith('/notifications/read-all')
  })
})
