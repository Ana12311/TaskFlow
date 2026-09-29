package com.example.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.taskflow.common.enums.NotificationType;
import com.example.taskflow.common.enums.Role;
import com.example.taskflow.common.exception.BusinessException;
import com.example.taskflow.common.result.ResultCode;
import com.example.taskflow.dto.TeamCreateRequest;
import com.example.taskflow.dto.TeamMemberAddRequest;
import com.example.taskflow.dto.TeamRoleUpdateRequest;
import com.example.taskflow.entity.Team;
import com.example.taskflow.entity.TeamMember;
import com.example.taskflow.entity.User;
import com.example.taskflow.mapper.TeamMapper;
import com.example.taskflow.mapper.TeamMemberMapper;
import com.example.taskflow.mapper.UserMapper;
import com.example.taskflow.service.NotificationService;
import com.example.taskflow.service.TeamService;
import com.example.taskflow.util.SecurityUtils;
import com.example.taskflow.vo.TeamDetailVO;
import com.example.taskflow.vo.TeamMemberVO;
import com.example.taskflow.vo.TeamVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 团队业务实现。所有成员管理操作都在这里做权限判断。
 * 核心思路：每个写操作先"我是谁"（SecurityUtils）→"我在不在这个团队"（requireTeamMember）→"我够不够格"（角色判断）。
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    private final TeamMemberMapper teamMemberMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public TeamServiceImpl(TeamMemberMapper teamMemberMapper, UserMapper userMapper,
                           NotificationService notificationService) {
        this.teamMemberMapper = teamMemberMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TeamVO createTeam(TeamCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. 建团队
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setOwnerId(userId);
        save(team); // 插入后 team.getId() 被回填

        // 2. 创建者自动成为 OWNER
        TeamMember owner = new TeamMember();
        owner.setTeamId(team.getId());
        owner.setUserId(userId);
        owner.setRole(Role.OWNER.name());
        teamMemberMapper.insert(owner);

        TeamVO vo = TeamVO.from(team);
        vo.setMyRole(Role.OWNER.name());
        vo.setMemberCount(1);
        return vo;
    }

    @Override
    public List<TeamVO> listMyTeams() {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. 查我所有的成员关系
        List<TeamMember> myMembers = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, userId));
        if (myMembers.isEmpty()) {
            return List.of();
        }

        Map<Long, String> roleByTeam = myMembers.stream()
                .collect(Collectors.toMap(TeamMember::getTeamId, TeamMember::getRole));
        List<Long> teamIds = myMembers.stream().map(TeamMember::getTeamId).toList();

        // 2. 一次查出这些团队的全部成员，统计每个团队的成员数
        Map<Long, Long> countByTeam = teamMemberMapper.selectList(
                        new LambdaQueryWrapper<TeamMember>().in(TeamMember::getTeamId, teamIds))
                .stream()
                .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        // 3. 查团队基础信息并组装
        return baseMapper.selectBatchIds(teamIds).stream().map(team -> {
            TeamVO vo = TeamVO.from(team);
            vo.setMyRole(roleByTeam.get(team.getId()));
            vo.setMemberCount(countByTeam.get(team.getId()).intValue());
            return vo;
        }).toList();
    }

    @Override
    public TeamDetailVO getTeamDetail(Long teamId) {
        Team team = requireTeam(teamId);
        // 非团队成员不能看团队内部信息
        requireTeamMember(teamId, SecurityUtils.getCurrentUserId());

        List<TeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId));

        TeamDetailVO vo = new TeamDetailVO();
        vo.setId(team.getId());
        vo.setName(team.getName());
        vo.setDescription(team.getDescription());
        vo.setOwnerId(team.getOwnerId());
        vo.setCreatedAt(team.getCreatedAt());
        vo.setMembers(buildMemberVOs(members));
        return vo;
    }

    @Override
    public void addMember(Long teamId, TeamMemberAddRequest request) {
        Team team = requireTeam(teamId);

        // 1. 当前用户必须是团队成员，且有管理权限
        TeamMember me = requireTeamMember(teamId, SecurityUtils.getCurrentUserId());
        Role myRole = parseRole(me.getRole());
        if (!myRole.canManageMembers()) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        // 2. 目标角色校验
        Role targetRole = request.getRole() == null ? Role.MEMBER : parseRole(request.getRole());
        if (targetRole == Role.OWNER) {
            // 团队只能有一个 OWNER，不能靠添加成员再造一个
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (myRole == Role.ADMIN && targetRole == Role.ADMIN) {
            // ADMIN 只能加 MEMBER，不能拉人当 ADMIN
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        // 3. 目标用户存在、且不在团队里
        if (userMapper.selectById(request.getUserId()) == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (getTeamMember(teamId, request.getUserId()) != null) {
            throw new BusinessException(ResultCode.ALREADY_TEAM_MEMBER);
        }

        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(request.getUserId());
        member.setRole(targetRole.name());
        teamMemberMapper.insert(member);
        // 通知被拉进团队的人
        notificationService.notify(request.getUserId(), NotificationType.TEAM_INVITED,
                "加入团队", "你已加入团队【" + team.getName() + "】", teamId);
    }

    @Override
    public void removeMember(Long teamId, Long userId) {
        requireTeam(teamId);

        TeamMember me = requireTeamMember(teamId, SecurityUtils.getCurrentUserId());
        Role myRole = parseRole(me.getRole());
        if (!myRole.canManageMembers()) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        TeamMember target = getTeamMember(teamId, userId);
        if (target == null) {
            throw new BusinessException(ResultCode.TEAM_MEMBER_NOT_FOUND);
        }
        Role targetRole = parseRole(target.getRole());
        if (targetRole == Role.OWNER) {
            // 不能移除 OWNER（也没有"踢出自己/转让"的语义）
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        if (myRole == Role.ADMIN && targetRole == Role.ADMIN) {
            // ADMIN 只能移除 MEMBER
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        teamMemberMapper.deleteById(target.getId());
    }

    @Override
    public void updateMemberRole(Long teamId, Long userId, TeamRoleUpdateRequest request) {
        requireTeam(teamId);

        // 只有 OWNER 能改角色
        TeamMember me = requireTeamMember(teamId, SecurityUtils.getCurrentUserId());
        if (parseRole(me.getRole()) != Role.OWNER) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        TeamMember target = getTeamMember(teamId, userId);
        if (target == null) {
            throw new BusinessException(ResultCode.TEAM_MEMBER_NOT_FOUND);
        }
        if (parseRole(target.getRole()) == Role.OWNER) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        Role newRole = parseRole(request.getRole());
        if (newRole == Role.OWNER) {
            // 不开放"转让 OWNER"
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        target.setRole(newRole.name());
        teamMemberMapper.updateById(target);
    }

    // ---------- 私有辅助方法 ----------

    /** 查团队，不存在抛异常 */
    private Team requireTeam(Long teamId) {
        Team team = baseMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ResultCode.TEAM_NOT_FOUND);
        }
        return team;
    }

    /** 查成员关系，查不到返回 null */
    private TeamMember getTeamMember(Long teamId, Long userId) {
        return teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
    }

    /** 查成员关系，查不到抛"不是团队成员" */
    private TeamMember requireTeamMember(Long teamId, Long userId) {
        TeamMember member = getTeamMember(teamId, userId);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_TEAM_MEMBER);
        }
        return member;
    }

    /** 角色字符串转枚举，非法值抛参数错误 */
    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "角色不合法");
        }
    }

    /** 成员关系列表 + 用户信息 -> 成员 VO 列表 */
    private List<TeamMemberVO> buildMemberVOs(List<TeamMember> members) {
        if (members.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = members.stream().map(TeamMember::getUserId).toList();
        Map<Long, User> userById = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return members.stream()
                .map(m -> TeamMemberVO.of(m, userById.get(m.getUserId())))
                .toList();
    }
}
