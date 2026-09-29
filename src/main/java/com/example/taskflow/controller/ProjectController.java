package com.example.taskflow.controller;

import com.example.taskflow.common.result.Result;
import com.example.taskflow.dto.ProjectCreateRequest;
import com.example.taskflow.dto.ProjectMemberAddRequest;
import com.example.taskflow.service.ProjectService;
import com.example.taskflow.vo.ProjectDetailVO;
import com.example.taskflow.vo.ProjectVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目接口。项目有两种路径前缀：
 * 团队维度：/api/teams/{teamId}/projects
 * 项目维度：/api/projects/{projectId}
 */
@RestController
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** 在团队下创建项目 */
    @PostMapping("/api/teams/{teamId}/projects")
    public Result<ProjectVO> createProject(@PathVariable Long teamId,
                                           @Valid @RequestBody ProjectCreateRequest request) {
        return Result.success(projectService.createProject(teamId, request));
    }

    /** 查询团队下的项目列表 */
    @GetMapping("/api/teams/{teamId}/projects")
    public Result<List<ProjectVO>> listTeamProjects(@PathVariable Long teamId) {
        return Result.success(projectService.listTeamProjects(teamId));
    }

    /** 查询项目详情（含成员列表） */
    @GetMapping("/api/projects/{projectId}")
    public Result<ProjectDetailVO> getProjectDetail(@PathVariable Long projectId) {
        return Result.success(projectService.getProjectDetail(projectId));
    }

    /** 添加项目成员 */
    @PostMapping("/api/projects/{projectId}/members")
    public Result<Void> addMember(@PathVariable Long projectId,
                                  @Valid @RequestBody ProjectMemberAddRequest request) {
        projectService.addMember(projectId, request);
        return Result.success();
    }

    /** 移除项目成员 */
    @DeleteMapping("/api/projects/{projectId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
        return Result.success();
    }

    /** 删除项目（级联删成员、任务、评论、日志、通知），仅创建者 */
    @DeleteMapping("/api/projects/{projectId}")
    public Result<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return Result.success();
    }
}
