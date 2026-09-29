package com.example.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.taskflow.common.enums.NotificationType;
import com.example.taskflow.common.exception.BusinessException;
import com.example.taskflow.common.result.ResultCode;
import com.example.taskflow.dto.CommentCreateRequest;
import com.example.taskflow.entity.ProjectMember;
import com.example.taskflow.entity.Task;
import com.example.taskflow.entity.TaskComment;
import com.example.taskflow.entity.User;
import com.example.taskflow.mapper.ProjectMemberMapper;
import com.example.taskflow.mapper.TaskCommentMapper;
import com.example.taskflow.mapper.TaskMapper;
import com.example.taskflow.mapper.UserMapper;
import com.example.taskflow.service.NotificationService;
import com.example.taskflow.service.TaskCommentService;
import com.example.taskflow.util.SecurityUtils;
import com.example.taskflow.vo.PageResult;
import com.example.taskflow.vo.TaskCommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务评论业务实现。
 * 只有任务所属项目的成员能发表/查看评论，只能删除自己的评论。
 */
@Service
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskComment> implements TaskCommentService {

    private final TaskMapper taskMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public TaskCommentServiceImpl(TaskMapper taskMapper,
                                  ProjectMemberMapper projectMemberMapper,
                                  UserMapper userMapper,
                                  NotificationService notificationService) {
        this.taskMapper = taskMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TaskCommentVO createComment(Long taskId, CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = requireTask(taskId);
        requireProjectMember(task.getProjectId(), userId);

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        save(comment);
        // 通知任务创建者和负责人（排除评论者自己）
        notificationService.notifyTaskMembers(task, userId, NotificationType.TASK_COMMENTED,
                "任务有新评论", "【" + task.getTitle() + "】有新评论");
        return buildVO(comment);
    }

    @Override
    public PageResult<TaskCommentVO> listComments(Long taskId, long pageNum, long pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = requireTask(taskId);
        requireProjectMember(task.getProjectId(), userId);

        Page<TaskComment> page = page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<TaskComment>()
                .eq(TaskComment::getTaskId, taskId)
                .orderByAsc(TaskComment::getCreatedAt));
        return PageResult.of(page, buildVOs(page.getRecords()));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        TaskComment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        removeById(commentId);
    }

    // ---------- 私有辅助方法 ----------

    private Task requireTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private void requireProjectMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_PROJECT_MEMBER);
        }
    }

    private TaskCommentVO buildVO(TaskComment comment) {
        User user = userMapper.selectById(comment.getUserId());
        return TaskCommentVO.from(comment, user);
    }

    private List<TaskCommentVO> buildVOs(List<TaskComment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = comments.stream().map(TaskComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userById = userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return comments.stream()
                .map(c -> TaskCommentVO.from(c, userById.get(c.getUserId())))
                .toList();
    }
}
