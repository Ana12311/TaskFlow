package com.example.taskflow.vo;

import com.example.taskflow.entity.Team;

import java.time.LocalDateTime;

/**
 * 团队信息响应对象（列表/创建用）。
 * myRole 表示"当前用户在这个团队里的角色"，memberCount 是成员数。
 */
public class TeamVO {

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String myRole;
    private Integer memberCount;
    private LocalDateTime createdAt;

    /** 从 Team 实体拷贝基础字段，myRole / memberCount 由 Service 补充 */
    public static TeamVO from(Team team) {
        TeamVO vo = new TeamVO();
        vo.setId(team.getId());
        vo.setName(team.getName());
        vo.setDescription(team.getDescription());
        vo.setOwnerId(team.getOwnerId());
        vo.setCreatedAt(team.getCreatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
