package com.example.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.taskflow.common.enums.NotificationType;
import com.example.taskflow.common.exception.BusinessException;
import com.example.taskflow.common.result.ResultCode;
import com.example.taskflow.entity.Notification;
import com.example.taskflow.entity.Task;
import com.example.taskflow.mapper.NotificationMapper;
import com.example.taskflow.service.NotificationService;
import com.example.taskflow.util.SecurityUtils;
import com.example.taskflow.vo.NotificationVO;
import com.example.taskflow.vo.PageResult;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 通知业务实现。发送通知只在业务操作处手动调用（与操作日志同一思路）。
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public void notify(Long userId, NotificationType type, String title, String content, Long relatedId) {
        if (userId == null) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type.name());
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        save(n);
    }

    @Override
    public void notifyTaskMembers(Task task, Long operatorId, NotificationType type, String title, String content) {
        Set<Long> recipients = new HashSet<>();
        if (task.getCreatorId() != null) {
            recipients.add(task.getCreatorId());
        }
        if (task.getAssigneeId() != null) {
            recipients.add(task.getAssigneeId());
        }
        recipients.remove(operatorId); // 自己操作不通知自己
        for (Long userId : recipients) {
            notify(userId, type, title, content, task.getId());
        }
    }

    @Override
    public PageResult<NotificationVO> listMyNotifications(long pageNum, long pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Notification> page = page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt));
        return PageResult.of(page, page.getRecords().stream().map(NotificationVO::from).toList());
    }

    @Override
    public long unreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long notificationId) {
        Notification n = getById(notificationId);
        if (n == null) {
            throw new BusinessException(ResultCode.NOTIFICATION_NOT_FOUND);
        }
        if (!n.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (n.getIsRead() == 0) {
            n.setIsRead(1);
            updateById(n);
        }
    }

    @Override
    public void markAllRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        update(new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }
}
