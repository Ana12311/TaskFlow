package com.example.taskflow.dto;

/**
 * 任务状态统计结果（Mapper GROUP BY 查询投影），非对外响应对象。
 */
public class TaskStatusCount {

    private String status;

    private Integer count;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
