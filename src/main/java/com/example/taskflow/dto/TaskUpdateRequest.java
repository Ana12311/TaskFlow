package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 修改任务内容请求体。只改标题/描述/截止日期，不改负责人和优先级（走专门接口）。
 */
public class TaskUpdateRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "标题最长 100 字符")
    private String title;

    @Size(max = 2000, message = "描述最长 2000 字符")
    private String description;

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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
