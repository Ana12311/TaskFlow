package com.example.taskflow.controller;

import com.example.taskflow.common.result.Result;
import com.example.taskflow.dto.TeamCreateRequest;
import com.example.taskflow.dto.TeamMemberAddRequest;
import com.example.taskflow.dto.TeamRoleUpdateRequest;
import com.example.taskflow.service.TeamService;
import com.example.taskflow.vo.TeamDetailVO;
import com.example.taskflow.vo.TeamVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 团队接口。Controller 只负责接请求、调 Service、返回 Result，权限判断全在 Service。
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /** 创建团队 */
    @PostMapping
    public Result<TeamVO> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        return Result.success(teamService.createTeam(request));
    }

    /** 查询当前用户所在的团队 */
    @GetMapping
    public Result<List<TeamVO>> listMyTeams() {
        return Result.success(teamService.listMyTeams());
    }

    /** 查询团队详情（含成员列表） */
    @GetMapping("/{teamId}")
    public Result<TeamDetailVO> getTeamDetail(@PathVariable Long teamId) {
        return Result.success(teamService.getTeamDetail(teamId));
    }

    /** 添加团队成员 */
    @PostMapping("/{teamId}/members")
    public Result<Void> addMember(@PathVariable Long teamId,
                                  @Valid @RequestBody TeamMemberAddRequest request) {
        teamService.addMember(teamId, request);
        return Result.success();
    }

    /** 移除团队成员 */
    @DeleteMapping("/{teamId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
        return Result.success();
    }

    /** 修改成员角色 */
    @PutMapping("/{teamId}/members/{userId}/role")
    public Result<Void> updateMemberRole(@PathVariable Long teamId,
                                         @PathVariable Long userId,
                                         @Valid @RequestBody TeamRoleUpdateRequest request) {
        teamService.updateMemberRole(teamId, userId, request);
        return Result.success();
    }
}
