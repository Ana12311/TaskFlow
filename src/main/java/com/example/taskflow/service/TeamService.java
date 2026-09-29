package com.example.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.taskflow.dto.TeamCreateRequest;
import com.example.taskflow.dto.TeamMemberAddRequest;
import com.example.taskflow.dto.TeamRoleUpdateRequest;
import com.example.taskflow.entity.Team;
import com.example.taskflow.vo.TeamDetailVO;
import com.example.taskflow.vo.TeamVO;

import java.util.List;

/**
 * 团队业务接口。团队 + 成员管理，含权限判断。
 */
public interface TeamService extends IService<Team> {

    /** 创建团队，创建者自动成为 OWNER */
    TeamVO createTeam(TeamCreateRequest request);

    /** 查询当前用户所在的团队列表 */
    List<TeamVO> listMyTeams();

    /** 查询团队详情（含成员列表），仅团队成员可见 */
    TeamDetailVO getTeamDetail(Long teamId);

    /** 添加成员，需要 OWNER/ADMIN */
    void addMember(Long teamId, TeamMemberAddRequest request);

    /** 移除成员，需要 OWNER/ADMIN（ADMIN 只能移除 MEMBER） */
    void removeMember(Long teamId, Long userId);

    /** 修改成员角色，仅 OWNER */
    void updateMemberRole(Long teamId, Long userId, TeamRoleUpdateRequest request);
}
