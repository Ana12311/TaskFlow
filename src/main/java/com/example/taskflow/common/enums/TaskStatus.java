package com.example.taskflow.common.enums;

/**
 * 任务状态枚举 + 状态机。
 *
 * 允许的流转：
 *   TODO        -> IN_PROGRESS, CANCELLED
 *   IN_PROGRESS -> TODO, DONE, CANCELLED
 *   DONE        -> CANCELLED
 *   CANCELLED   -> TODO
 *
 * 两条硬规则：
 *   1. DONE 不能回到 TODO（已完成不可重开为待办）
 *   2. CANCELLED 不能进入 IN_PROGRESS（已取消不可直接进行中，只能先回到 TODO）
 */
public enum TaskStatus {

    TODO,
    IN_PROGRESS,
    DONE,
    CANCELLED;

    /**
     * 判断当前状态能否流转到目标状态。
     * 状态流转校验统一走这里，Service 层调用，禁止在 Controller 里手写判断。
     */
    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case TODO        -> target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS -> target == TODO || target == DONE || target == CANCELLED;
            case DONE        -> target == CANCELLED;
            case CANCELLED   -> target == TODO;
        };
    }
}
