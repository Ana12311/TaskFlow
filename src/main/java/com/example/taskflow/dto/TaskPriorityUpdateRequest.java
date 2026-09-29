package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改任务优先级请求体。
 */
public class TaskPriorityUpdateRequest {

    @NotBlank(message = "任务优先级不能为空")
    private String priority;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
