package com.example.taskflow.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队详情响应对象：团队基础信息 + 成员列表。
 */
public class TeamDetailVO {

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDateTime createdAt;
    private List<TeamMemberVO> members;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TeamMemberVO> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMemberVO> members) {
        this.members = members;
    }
}
