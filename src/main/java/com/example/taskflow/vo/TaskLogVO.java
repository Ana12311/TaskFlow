package com.example.taskflow.vo;

import com.example.taskflow.entity.TaskLog;
import com.example.taskflow.entity.User;

import java.time.LocalDateTime;

/**
 * 任务操作日志响应对象，带操作人昵称。
 */
public class TaskLogVO {

    private Long id;
    private Long taskId;
    private Long operatorId;
    private String operatorName;
    private String operationType;
    private String beforeValue;
    private String afterValue;
    private LocalDateTime createdAt;

    public static TaskLogVO from(TaskLog log, User operator) {
        TaskLogVO vo = new TaskLogVO();
        vo.setId(log.getId());
        vo.setTaskId(log.getTaskId());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorName(operator == null ? null : operator.getNickname());
        vo.setOperationType(log.getOperationType());
        vo.setBeforeValue(log.getBeforeValue());
        vo.setAfterValue(log.getAfterValue());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
