package com.example.taskflow.vo;

import com.example.taskflow.entity.Task;
import com.example.taskflow.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务信息响应对象。带创建人/负责人昵称，便于前端直接展示。
 */
public class TaskVO {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private Long creatorId;
    private String creatorName;
    private Long assigneeId;
    private String assigneeName;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Entity -> VO，creator / assignee 可能为 null（负责人未分配） */
    public static TaskVO from(Task task, User creator, User assignee) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setProjectId(task.getProjectId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setCreatorId(task.getCreatorId());
        vo.setCreatorName(creator == null ? null : creator.getNickname());
        vo.setAssigneeId(task.getAssigneeId());
        vo.setAssigneeName(assignee == null ? null : assignee.getNickname());
        vo.setPriority(task.getPriority());
        vo.setStatus(task.getStatus());
        vo.setDueDate(task.getDueDate());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
