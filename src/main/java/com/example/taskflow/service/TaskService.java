package com.example.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.taskflow.dto.TaskAssignRequest;
import com.example.taskflow.dto.TaskCreateRequest;
import com.example.taskflow.dto.TaskPriorityUpdateRequest;
import com.example.taskflow.dto.TaskStatusUpdateRequest;
import com.example.taskflow.dto.TaskUpdateRequest;
import com.example.taskflow.entity.Task;
import com.example.taskflow.vo.PageResult;
import com.example.taskflow.vo.TaskLogVO;
import com.example.taskflow.vo.TaskStatsVO;
import com.example.taskflow.vo.TaskVO;

/**
 * 任务业务接口。状态流转校验、权限判断都在 Service 层。
 */
public interface TaskService extends IService<Task> {

    /** 创建任务，需是项目成员，负责人必须属项目 */
    TaskVO createTask(Long projectId, TaskCreateRequest request);

    /** 修改任务内容（标题/描述/截止日期），创建者/负责人/项目管理者可改 */
    TaskVO updateTask(Long taskId, TaskUpdateRequest request);

    /** 删除任务，创建者或项目管理者可删 */
    void deleteTask(Long taskId);

    /** 查询任务详情，仅项目成员可见 */
    TaskVO getTaskDetail(Long taskId);

    /** 分页查询项目任务列表，仅项目成员可见 */
    PageResult<TaskVO> listProjectTasks(Long projectId, long pageNum, long pageSize);

    /** 查询项目任务统计（各状态数量），带缓存，仅项目成员可见 */
    TaskStatsVO getProjectStats(Long projectId);

    /** 分配负责人，创建者或项目管理者可操作，负责人需属项目 */
    TaskVO assignTask(Long taskId, TaskAssignRequest request);

    /** 修改状态，走 canTransitionTo 校验 */
    TaskVO updateStatus(Long taskId, TaskStatusUpdateRequest request);

    /** 修改优先级，创建者或项目管理者可操作 */
    TaskVO updatePriority(Long taskId, TaskPriorityUpdateRequest request);

    /** 分页查询任务操作日志列表，仅项目成员可见 */
    PageResult<TaskLogVO> listTaskLogs(Long taskId, long pageNum, long pageSize);
}
