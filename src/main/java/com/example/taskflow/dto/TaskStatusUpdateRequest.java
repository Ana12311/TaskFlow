package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改任务状态请求体。
 */
public class TaskStatusUpdateRequest {

    @NotBlank(message = "任务状态不能为空")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
