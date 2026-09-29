package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 创建任务请求体。创建者自动成为任务创建人，初始状态 TODO。
 */
public class TaskCreateRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "标题最长 100 字符")
    private String title;

    @Size(max = 2000, message = "描述最长 2000 字符")
    private String description;

    /** 负责人用户 id，可空 */
    private Long assigneeId;

    /** 优先级字符串，取值 LOW/MEDIUM/HIGH/URGENT，可空默认 MEDIUM */
    private String priority;

    /** 截止日期，可空 */
    private LocalDate dueDate;

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

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
