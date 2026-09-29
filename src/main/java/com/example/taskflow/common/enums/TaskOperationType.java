package com.example.taskflow.common.enums;

/**
 * 任务操作类型枚举，用于操作日志。
 */
public enum TaskOperationType {

    /** 创建任务 */
    CREATE,

    /** 修改负责人 */
    ASSIGNEE_CHANGED,

    /** 修改状态 */
    STATUS_CHANGED,

    /** 修改优先级 */
    PRIORITY_CHANGED,

    /** 删除任务 */
    DELETE
}
