package com.example.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.taskflow.common.enums.NotificationType;
import com.example.taskflow.entity.Notification;
import com.example.taskflow.entity.Task;
import com.example.taskflow.vo.NotificationVO;
import com.example.taskflow.vo.PageResult;

/**
 * 通知业务接口。
 */
public interface NotificationService extends IService<Notification> {

    /** 发给单个用户（userId 为 null 则跳过，比如任务没负责人） */
    void notify(Long userId, NotificationType type, String title, String content, Long relatedId);

    /** 发给任务的创建者和负责人（去重，排除 operatorId 自己） */
    void notifyTaskMembers(Task task, Long operatorId, NotificationType type, String title, String content);

    /** 分页查询我的通知列表（倒序） */
    PageResult<NotificationVO> listMyNotifications(long pageNum, long pageSize);

    /** 我的未读数 */
    long unreadCount();

    /** 标记单条已读（只能标记自己的） */
    void markRead(Long notificationId);

    /** 全部标记已读 */
    void markAllRead();
}
