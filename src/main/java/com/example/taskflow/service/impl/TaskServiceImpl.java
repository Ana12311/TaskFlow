package com.example.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.taskflow.common.cache.CacheKeys;
import com.example.taskflow.common.cache.CacheService;
import com.example.taskflow.common.enums.NotificationType;
import com.example.taskflow.common.enums.Role;
import com.example.taskflow.common.enums.TaskOperationType;
import com.example.taskflow.common.enums.TaskPriority;
import com.example.taskflow.common.enums.TaskStatus;
import com.example.taskflow.common.exception.BusinessException;
import com.example.taskflow.common.result.ResultCode;
import com.example.taskflow.dto.TaskAssignRequest;
import com.example.taskflow.dto.TaskCreateRequest;
import com.example.taskflow.dto.TaskPriorityUpdateRequest;
import com.example.taskflow.dto.TaskStatusUpdateRequest;
import com.example.taskflow.dto.TaskStatusCount;
import com.example.taskflow.dto.TaskUpdateRequest;
import com.example.taskflow.entity.Project;
import com.example.taskflow.entity.ProjectMember;
import com.example.taskflow.entity.Task;
import com.example.taskflow.entity.TaskLog;
import com.example.taskflow.entity.User;
import com.example.taskflow.mapper.ProjectMapper;
import com.example.taskflow.mapper.ProjectMemberMapper;
import com.example.taskflow.mapper.TaskLogMapper;
import com.example.taskflow.mapper.TaskMapper;
import com.example.taskflow.mapper.UserMapper;
import com.example.taskflow.service.NotificationService;
import com.example.taskflow.service.TaskService;
import com.example.taskflow.util.SecurityUtils;
import com.example.taskflow.vo.PageResult;
import com.example.taskflow.vo.TaskLogVO;
import com.example.taskflow.vo.TaskStatsVO;
import com.example.taskflow.vo.TaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务业务实现。
 * 权限分两档：参与级（创建者/负责人/项目管理者）能改内容、改状态；
 * 管理级（创建者/项目管理者）能改优先级、分配负责人、删除。
 * 状态流转统一走 TaskStatus.canTransitionTo。
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final CacheService cacheService;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final TaskLogMapper taskLogMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public TaskServiceImpl(CacheService cacheService,
                           ProjectMapper projectMapper,
                           ProjectMemberMapper projectMemberMapper,
                           TaskLogMapper taskLogMapper,
                           UserMapper userMapper,
                           NotificationService notificationService) {
        this.cacheService = cacheService;
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.taskLogMapper = taskLogMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TaskVO createTask(Long projectId, TaskCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        requireProject(projectId);
        requireProjectMember(projectId, userId);

        Long assigneeId = request.getAssigneeId();
        if (assigneeId != null) {
            requireAssigneeInProject(projectId, assigneeId);
        }

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreatorId(userId);
        task.setAssigneeId(assigneeId);
        task.setPriority(request.getPriority() == null
                ? TaskPriority.MEDIUM.name()
                : parsePriority(request.getPriority()).name());
        task.setStatus(TaskStatus.TODO.name());
        task.setDueDate(request.getDueDate());
        save(task);
        recordTaskLog(task.getId(), userId, TaskOperationType.CREATE, null, task.getTitle());
        cacheService.delete(CacheKeys.PROJECT_STATS + projectId);
        return buildTaskVO(task);
    }

    @Override
    @Transactional
    public TaskVO updateTask(Long taskId, TaskUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = requireTask(taskId);
        requireCanEdit(task, userId);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        updateById(task);
        return buildTaskVO(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = requireTask(taskId);
        requireCanManage(task, userId);
        String title = task.getTitle();
        removeById(taskId);
        recordTaskLog(taskId, userId, TaskOperationType.DELETE, title, null);
        cacheService.delete(CacheKeys.PROJECT_STATS + task.getProjectId());
    }

    @Override
    public TaskVO getTaskDetail(Long taskId) {
        Task task = requireTask(taskId);
        requireProjectMember(task.getProjectId(), SecurityUtils.getCurrentUserId());
        return buildTaskVO(task);
    }

    @Override
    public PageResult<TaskVO> listProjectTasks(Long projectId, long pageNum, long pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        requireProject(projectId);
        requireProjectMember(projectId, userId);

        Page<Task> page = page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Task>()
                .eq(Task::getProjectId, projectId)
                .orderByDesc(Task::getCreatedAt));
        return PageResult.of(page, buildTaskVOs(page.getRecords()));
    }

    @Override
    public TaskStatsVO getProjectStats(Long projectId) {
        requireProject(projectId);
        requireProjectMember(projectId, SecurityUtils.getCurrentUserId());

        String key = CacheKeys.PROJECT_STATS + projectId;
        TaskStatsVO cached = cacheService.get(key, TaskStatsVO.class);
        if (cached != null) {
            return cached;
        }
        TaskStatsVO vo = computeStats(projectId);
        cacheService.set(key, vo, Duration.ofMinutes(10));
        return vo;
    }

    @Override
    @Transactional
    public TaskVO assignTask(Long taskId, TaskAssignRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = requireTask(taskId);
        requireCanManage(task, userId);

        Long assigneeId = request.getAssigneeId();
        if (assigneeId != null) {
            requireAssigneeInProject(task.getProjectId(), assigneeId);
        }
        String before = str(task.getAssigneeId());
        task.setAssigneeId(assigneeId);
        updateById(task);
        recordTaskLog(taskId, userId, TaskOperationType.ASSIGNEE_CHANGED, before, str(assigneeId));
        // 通知新负责人（自己分配给自己不通知）
        if (assigneeId != null && !assigneeId.equals(userId)) {
            notificationService.notify(assigneeId, NotificationType.TASK_ASSIGNED,
                    "任务分配给你", "【" + task.getTitle() + "】已分配给你", taskId);
        }
        return buildTaskVO(task);
    }

    @Override
    @Transactional
    public TaskVO updateStatus(Long taskId, TaskStatusUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = requireTask(taskId);
        requireCanEdit(task, userId);

        TaskStatus current = parseStatus(task.getStatus());
        TaskStatus target = parseStatus(request.getStatus());
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(ResultCode.ILLEGAL_STATUS_TRANSITION);
        }
        String before = task.getStatus();
        task.setStatus(target.name());
        updateById(task);
        recordTaskLog(taskId, userId, TaskOperationType.STATUS_CHANGED, before, target.name());
        cacheService.delete(CacheKeys.PROJECT_STATS + task.getProjectId());
        // 通知创建者和负责人（排除操作者自己）
        notificationService.notifyTaskMembers(task, userId, NotificationType.TASK_STATUS_CHANGED,
                "任务状态变更", "【" + task.getTitle() + "】状态已变更");
        return buildTaskVO(task);
    }

    @Override
    @Transactional
    public TaskVO updatePriority(Long taskId, TaskPriorityUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = requireTask(taskId);
        requireCanManage(task, userId);

        String before = task.getPriority();
        task.setPriority(parsePriority(request.getPriority()).name());
        updateById(task);
        recordTaskLog(taskId, userId, TaskOperationType.PRIORITY_CHANGED, before, task.getPriority());
        return buildTaskVO(task);
    }

    @Override
    public PageResult<TaskLogVO> listTaskLogs(Long taskId, long pageNum, long pageSize) {
        Task task = requireTask(taskId);
        requireProjectMember(task.getProjectId(), SecurityUtils.getCurrentUserId());
        Page<TaskLog> page = taskLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TaskLog>()
                        .eq(TaskLog::getTaskId, taskId)
                        .orderByDesc(TaskLog::getCreatedAt));
        return PageResult.of(page, buildTaskLogVOs(page.getRecords()));
    }

    // ---------- 私有辅助方法 ----------

    private Task requireTask(Long taskId) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private Project requireProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    private ProjectMember getProjectMember(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private void requireProjectMember(Long projectId, Long userId) {
        if (getProjectMember(projectId, userId) == null) {
            throw new BusinessException(ResultCode.NOT_PROJECT_MEMBER);
        }
    }

    private void requireAssigneeInProject(Long projectId, Long assigneeId) {
        if (getProjectMember(projectId, assigneeId) == null) {
            throw new BusinessException(ResultCode.ASSIGNEE_NOT_IN_PROJECT);
        }
    }

    /** 项目管理者：项目成员中 role 为 OWNER 或 ADMIN */
    private boolean isProjectManager(Long projectId, Long userId) {
        ProjectMember member = getProjectMember(projectId, userId);
        return member != null && parseRole(member.getRole()).canManageMembers();
    }

    private boolean isCreator(Task task, Long userId) {
        return task.getCreatorId().equals(userId);
    }

    private boolean isAssignee(Task task, Long userId) {
        return task.getAssigneeId() != null && task.getAssigneeId().equals(userId);
    }

    /** 参与级：创建者 / 负责人 / 项目管理者 */
    private void requireCanEdit(Task task, Long userId) {
        if (isCreator(task, userId) || isAssignee(task, userId)
                || isProjectManager(task.getProjectId(), userId)) {
            return;
        }
        throw new BusinessException(ResultCode.NO_PERMISSION);
    }

    /** 管理级：创建者 / 项目管理者 */
    private void requireCanManage(Task task, Long userId) {
        if (isCreator(task, userId) || isProjectManager(task.getProjectId(), userId)) {
            return;
        }
        throw new BusinessException(ResultCode.NO_PERMISSION);
    }

    private TaskPriority parsePriority(String priority) {
        try {
            return TaskPriority.valueOf(priority);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "优先级不合法");
        }
    }

    private TaskStatus parseStatus(String status) {
        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "任务状态不合法");
        }
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "角色不合法");
        }
    }

    private TaskVO buildTaskVO(Task task) {
        Set<Long> ids = new HashSet<>();
        ids.add(task.getCreatorId());
        if (task.getAssigneeId() != null) {
            ids.add(task.getAssigneeId());
        }
        Map<Long, User> userById = loadUsers(new ArrayList<>(ids));
        return toVO(task, userById);
    }

    private List<TaskVO> buildTaskVOs(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = new HashSet<>();
        for (Task task : tasks) {
            ids.add(task.getCreatorId());
            if (task.getAssigneeId() != null) {
                ids.add(task.getAssigneeId());
            }
        }
        Map<Long, User> userById = loadUsers(new ArrayList<>(ids));
        return tasks.stream()
                .map(task -> toVO(task, userById))
                .toList();
    }

    private Map<Long, User> loadUsers(List<Long> ids) {
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private TaskVO toVO(Task task, Map<Long, User> userById) {
        User creator = task.getCreatorId() == null ? null : userById.get(task.getCreatorId());
        User assignee = task.getAssigneeId() == null ? null : userById.get(task.getAssigneeId());
        return TaskVO.from(task, creator, assignee);
    }

    /** 记录任务操作日志，业务成功后调用（同一事务内） */
    private void recordTaskLog(Long taskId, Long operatorId, TaskOperationType type,
                               String before, String after) {
        TaskLog log = new TaskLog();
        log.setTaskId(taskId);
        log.setOperatorId(operatorId);
        log.setOperationType(type.name());
        log.setBeforeValue(before);
        log.setAfterValue(after);
        taskLogMapper.insert(log);
    }

    private List<TaskLogVO> buildTaskLogVOs(List<TaskLog> logs) {
        if (logs.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = logs.stream().map(TaskLog::getOperatorId).collect(Collectors.toSet());
        Map<Long, User> userById = loadUsers(new ArrayList<>(ids));
        return logs.stream()
                .map(log -> TaskLogVO.from(log, userById.get(log.getOperatorId())))
                .toList();
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /** 从数据库统计各状态任务数，供统计缓存回源使用 */
    private TaskStatsVO computeStats(Long projectId) {
        List<TaskStatusCount> rows = baseMapper.countByStatus(projectId);
        Map<String, Long> byStatus = rows.stream().collect(Collectors.toMap(
                TaskStatusCount::getStatus,
                r -> r.getCount() == null ? 0L : r.getCount().longValue()));
        long todo = byStatus.getOrDefault("TODO", 0L);
        long inProgress = byStatus.getOrDefault("IN_PROGRESS", 0L);
        long done = byStatus.getOrDefault("DONE", 0L);
        long cancelled = byStatus.getOrDefault("CANCELLED", 0L);

        TaskStatsVO vo = new TaskStatsVO();
        vo.setTotal(todo + inProgress + done + cancelled);
        vo.setTodo(todo);
        vo.setInProgress(inProgress);
        vo.setDone(done);
        vo.setCancelled(cancelled);
        return vo;
    }
}
