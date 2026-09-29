package com.example.taskflow.controller;

import com.example.taskflow.common.result.Result;
import com.example.taskflow.dto.TaskAssignRequest;
import com.example.taskflow.dto.TaskCreateRequest;
import com.example.taskflow.dto.TaskPriorityUpdateRequest;
import com.example.taskflow.dto.TaskStatusUpdateRequest;
import com.example.taskflow.dto.TaskUpdateRequest;
import com.example.taskflow.service.TaskService;
import com.example.taskflow.vo.PageResult;
import com.example.taskflow.vo.TaskLogVO;
import com.example.taskflow.vo.TaskStatsVO;
import com.example.taskflow.vo.TaskVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务接口。两种路径前缀：
 * 项目维度：/api/projects/{projectId}/tasks（创建、列表）
 * 任务维度：/api/tasks/{taskId}（详情、修改、删除、分配、状态、优先级）
 * Controller 只接参数调 Service，零业务判断。
 */
@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** 创建任务 */
    @PostMapping("/api/projects/{projectId}/tasks")
    public Result<TaskVO> createTask(@PathVariable Long projectId,
                                     @Valid @RequestBody TaskCreateRequest request) {
        return Result.success(taskService.createTask(projectId, request));
    }

    /** 查询项目任务列表（分页） */
    @GetMapping("/api/projects/{projectId}/tasks")
    public Result<PageResult<TaskVO>> listProjectTasks(@PathVariable Long projectId,
                                                       @RequestParam(defaultValue = "1") long pageNum,
                                                       @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(taskService.listProjectTasks(projectId, pageNum, pageSize));
    }

    /** 查询项目任务统计（带缓存） */
    @GetMapping("/api/projects/{projectId}/stats")
    public Result<TaskStatsVO> getProjectStats(@PathVariable Long projectId) {
        return Result.success(taskService.getProjectStats(projectId));
    }

    /** 查询任务详情 */
    @GetMapping("/api/tasks/{taskId}")
    public Result<TaskVO> getTaskDetail(@PathVariable Long taskId) {
        return Result.success(taskService.getTaskDetail(taskId));
    }

    /** 修改任务内容 */
    @PutMapping("/api/tasks/{taskId}")
    public Result<TaskVO> updateTask(@PathVariable Long taskId,
                                     @Valid @RequestBody TaskUpdateRequest request) {
        return Result.success(taskService.updateTask(taskId, request));
    }

    /** 删除任务 */
    @DeleteMapping("/api/tasks/{taskId}")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return Result.success();
    }

    /** 分配负责人 */
    @PutMapping("/api/tasks/{taskId}/assignee")
    public Result<TaskVO> assignTask(@PathVariable Long taskId,
                                     @RequestBody TaskAssignRequest request) {
        return Result.success(taskService.assignTask(taskId, request));
    }

    /** 修改状态 */
    @PutMapping("/api/tasks/{taskId}/status")
    public Result<TaskVO> updateStatus(@PathVariable Long taskId,
                                       @Valid @RequestBody TaskStatusUpdateRequest request) {
        return Result.success(taskService.updateStatus(taskId, request));
    }

    /** 修改优先级 */
    @PutMapping("/api/tasks/{taskId}/priority")
    public Result<TaskVO> updatePriority(@PathVariable Long taskId,
                                         @Valid @RequestBody TaskPriorityUpdateRequest request) {
        return Result.success(taskService.updatePriority(taskId, request));
    }

    /** 查询任务操作日志列表（分页） */
    @GetMapping("/api/tasks/{taskId}/logs")
    public Result<PageResult<TaskLogVO>> listTaskLogs(@PathVariable Long taskId,
                                                      @RequestParam(defaultValue = "1") long pageNum,
                                                      @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(taskService.listTaskLogs(taskId, pageNum, pageSize));
    }
}
