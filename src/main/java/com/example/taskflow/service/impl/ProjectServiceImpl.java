package com.example.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.taskflow.common.cache.CacheKeys;
import com.example.taskflow.common.cache.CacheService;
import com.example.taskflow.common.enums.NotificationType;
import com.example.taskflow.common.enums.Role;
import com.example.taskflow.common.exception.BusinessException;
import com.example.taskflow.common.result.ResultCode;
import com.example.taskflow.dto.ProjectCreateRequest;
import com.example.taskflow.dto.ProjectMemberAddRequest;
import com.example.taskflow.entity.Notification;
import com.example.taskflow.entity.Project;
import com.example.taskflow.entity.ProjectMember;
import com.example.taskflow.entity.Task;
import com.example.taskflow.entity.TaskComment;
import com.example.taskflow.entity.TaskLog;
import com.example.taskflow.entity.TeamMember;
import com.example.taskflow.entity.User;
import com.example.taskflow.mapper.NotificationMapper;
import com.example.taskflow.mapper.ProjectMapper;
import com.example.taskflow.mapper.ProjectMemberMapper;
import com.example.taskflow.mapper.TaskCommentMapper;
import com.example.taskflow.mapper.TaskLogMapper;
import com.example.taskflow.mapper.TaskMapper;
import com.example.taskflow.mapper.TeamMapper;
import com.example.taskflow.mapper.TeamMemberMapper;
import com.example.taskflow.mapper.UserMapper;
import com.example.taskflow.service.NotificationService;
import com.example.taskflow.service.ProjectService;
import com.example.taskflow.util.SecurityUtils;
import com.example.taskflow.vo.ProjectDetailVO;
import com.example.taskflow.vo.ProjectMemberVO;
import com.example.taskflow.vo.ProjectVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目业务实现。
 * 项目归属团队：先校验团队关系，再校验项目成员关系，最后才是权限判断。
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final ProjectMemberMapper projectMemberMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamMapper teamMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final TaskMapper taskMapper;
    private final TaskCommentMapper taskCommentMapper;
    private final TaskLogMapper taskLogMapper;
    private final NotificationMapper notificationMapper;
    private final CacheService cacheService;

    public ProjectServiceImpl(ProjectMemberMapper projectMemberMapper,
                              TeamMemberMapper teamMemberMapper,
                              TeamMapper teamMapper,
                              UserMapper userMapper,
                              NotificationService notificationService,
                              TaskMapper taskMapper,
                              TaskCommentMapper taskCommentMapper,
                              TaskLogMapper taskLogMapper,
                              NotificationMapper notificationMapper,
                              CacheService cacheService) {
        this.projectMemberMapper = projectMemberMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.teamMapper = teamMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.taskMapper = taskMapper;
        this.taskCommentMapper = taskCommentMapper;
        this.taskLogMapper = taskLogMapper;
        this.notificationMapper = notificationMapper;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional
    public ProjectVO createProject(Long teamId, ProjectCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. 团队必须存在
        if (teamMapper.selectById(teamId) == null) {
            throw new BusinessException(ResultCode.TEAM_NOT_FOUND);
        }
        // 2. 任意团队成员都能建项目
        requireTeamMember(teamId, userId);

        // 3. 建项目，创建者自动成为项目 OWNER
        Project project = new Project();
        project.setTeamId(teamId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(1);
        project.setCreatedBy(userId);
        save(project);

        ProjectMember owner = new ProjectMember();
        owner.setProjectId(project.getId());
        owner.setUserId(userId);
        owner.setRole(Role.OWNER.name());
        projectMemberMapper.insert(owner);

        return ProjectVO.from(project);
    }

    @Override
    public List<ProjectVO> listTeamProjects(Long teamId) {
        requireTeamMember(teamId, SecurityUtils.getCurrentUserId());
        return baseMapper.selectList(
                        new LambdaQueryWrapper<Project>().eq(Project::getTeamId, teamId))
                .stream()
                .map(ProjectVO::from)
                .toList();
    }

    @Override
    public ProjectDetailVO getProjectDetail(Long projectId) {
        Project project = requireProject(projectId);
        Long userId = SecurityUtils.getCurrentUserId();
        // 只有项目成员能看项目内部信息
        if (getProjectMember(projectId, userId) == null) {
            throw new BusinessException(ResultCode.NOT_PROJECT_MEMBER);
        }

        List<ProjectMember> members = projectMemberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));

        ProjectDetailVO vo = new ProjectDetailVO();
        vo.setId(project.getId());
        vo.setTeamId(project.getTeamId());
        vo.setName(project.getName());
        vo.setDescription(project.getDescription());
        vo.setStatus(project.getStatus());
        vo.setCreatedBy(project.getCreatedBy());
        vo.setCreatedAt(project.getCreatedAt());
        vo.setMembers(buildMemberVOs(members));
        return vo;
    }

    @Override
    public void addMember(Long projectId, ProjectMemberAddRequest request) {
        Project project = requireProject(projectId);
        Long userId = SecurityUtils.getCurrentUserId();
        requireCanManageProjectMembers(userId, project);

        // 目标角色不能是 OWNER（创建者才是 OWNER）
        Role targetRole = request.getRole() == null ? Role.MEMBER : parseRole(request.getRole());
        if (targetRole == Role.OWNER) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        if (userMapper.selectById(request.getUserId()) == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (getProjectMember(projectId, request.getUserId()) != null) {
            throw new BusinessException(ResultCode.ALREADY_PROJECT_MEMBER);
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.getUserId());
        member.setRole(targetRole.name());
        projectMemberMapper.insert(member);
        // 通知被拉进项目的人
        notificationService.notify(request.getUserId(), NotificationType.PROJECT_INVITED,
                "加入项目", "你已加入项目【" + project.getName() + "】", projectId);
    }

    @Override
    public void removeMember(Long projectId, Long userId) {
        Project project = requireProject(projectId);
        Long me = SecurityUtils.getCurrentUserId();
        requireCanManageProjectMembers(me, project);

        ProjectMember target = getProjectMember(projectId, userId);
        if (target == null) {
            throw new BusinessException(ResultCode.PROJECT_MEMBER_NOT_FOUND);
        }
        if (parseRole(target.getRole()) == Role.OWNER) {
            // 不能移除项目创建者
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        projectMemberMapper.deleteById(target.getId());
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Project project = requireProject(projectId);
        // 只有项目创建者能删
        if (!project.getCreatedBy().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        // 1. 项目下所有任务 id，先级联删评论、日志、任务相关通知
        List<Long> taskIds = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                        .eq(Task::getProjectId, projectId))
                .stream().map(Task::getId).toList();
        if (!taskIds.isEmpty()) {
            taskCommentMapper.delete(new LambdaQueryWrapper<TaskComment>()
                    .in(TaskComment::getTaskId, taskIds));
            taskLogMapper.delete(new LambdaQueryWrapper<TaskLog>()
                    .in(TaskLog::getTaskId, taskIds));
            notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                    .in(Notification::getRelatedId, taskIds));
            taskMapper.delete(new LambdaQueryWrapper<Task>()
                    .eq(Task::getProjectId, projectId));
        }

        // 2. 删项目邀请通知（related_id 指向项目）
        notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getType, NotificationType.PROJECT_INVITED.name())
                .eq(Notification::getRelatedId, projectId));

        // 3. 删项目成员、项目本身
        projectMemberMapper.delete(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId));
        removeById(projectId);

        // 4. 清统计缓存
        cacheService.delete(CacheKeys.PROJECT_STATS + projectId);
    }

    // ---------- 私有辅助方法 ----------

    private Project requireProject(Long projectId) {
        Project project = baseMapper.selectById(projectId);
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

    private TeamMember getTeamMember(Long teamId, Long userId) {
        return teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
    }

    private void requireTeamMember(Long teamId, Long userId) {
        if (getTeamMember(teamId, userId) == null) {
            throw new BusinessException(ResultCode.NOT_TEAM_MEMBER);
        }
    }

    /**
     * 校验"能否管理项目成员"：
     * 项目创建者可以；否则必须是该项目的团队的 OWNER 或 ADMIN。
     */
    private void requireCanManageProjectMembers(Long userId, Project project) {
        if (project.getCreatedBy().equals(userId)) {
            return;
        }
        TeamMember teamMember = getTeamMember(project.getTeamId(), userId);
        if (teamMember == null || !parseRole(teamMember.getRole()).canManageMembers()) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "角色不合法");
        }
    }

    private List<ProjectMemberVO> buildMemberVOs(List<ProjectMember> members) {
        if (members.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = members.stream().map(ProjectMember::getUserId).toList();
        Map<Long, User> userById = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return members.stream()
                .map(m -> ProjectMemberVO.of(m, userById.get(m.getUserId())))
                .toList();
    }
}
