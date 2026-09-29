package com.example.taskflow.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目详情响应对象：项目基础信息 + 成员列表。
 */
public class ProjectDetailVO {

    private Long id;
    private Long teamId;
    private String name;
    private String description;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private List<ProjectMemberVO> members;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ProjectMemberVO> getMembers() {
        return members;
    }

    public void setMembers(List<ProjectMemberVO> members) {
        this.members = members;
    }
}
