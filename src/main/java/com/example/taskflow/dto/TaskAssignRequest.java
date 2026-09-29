package com.example.taskflow.dto;

/**
 * 分配任务负责人请求体。assigneeId 传 null 表示取消负责人。
 */
public class TaskAssignRequest {

    private Long assigneeId;

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
