package com.example.taskflow.vo;

import com.example.taskflow.entity.Project;

import java.time.LocalDateTime;

/**
 * 项目信息响应对象（列表/创建用）。
 */
public class ProjectVO {

    private Long id;
    private Long teamId;
    private String name;
    private String description;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static ProjectVO from(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setTeamId(project.getTeamId());
        vo.setName(project.getName());
        vo.setDescription(project.getDescription());
        vo.setStatus(project.getStatus());
        vo.setCreatedBy(project.getCreatedBy());
        vo.setCreatedAt(project.getCreatedAt());
        return vo;
    }

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
}
