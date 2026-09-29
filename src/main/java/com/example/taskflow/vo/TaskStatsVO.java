package com.example.taskflow.vo;

/**
 * 项目任务统计响应对象：各状态数量汇总。
 */
public class TaskStatsVO {

    private Long total;
    private Long todo;
    private Long inProgress;
    private Long done;
    private Long cancelled;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTodo() {
        return todo;
    }

    public void setTodo(Long todo) {
        this.todo = todo;
    }

    public Long getInProgress() {
        return inProgress;
    }

    public void setInProgress(Long inProgress) {
        this.inProgress = inProgress;
    }

    public Long getDone() {
        return done;
    }

    public void setDone(Long done) {
        this.done = done;
    }

    public Long getCancelled() {
        return cancelled;
    }

    public void setCancelled(Long cancelled) {
        this.cancelled = cancelled;
    }
}
