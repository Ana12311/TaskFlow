package com.example.taskflow.common.enums;

/**
 * 通知类型枚举。relatedId 的含义由类型决定：
 * TASK_* -> 任务 id；TEAM_INVITED -> 团队 id；PROJECT_INVITED -> 项目 id。
 */
public enum NotificationType {

    /** 任务分配给你 */
    TASK_ASSIGNED,

    /** 你的任务被评论 */
    TASK_COMMENTED,

    /** 你的任务状态变更 */
    TASK_STATUS_CHANGED,

    /** 被加入团队 */
    TEAM_INVITED,

    /** 被加入项目 */
    PROJECT_INVITED
}
