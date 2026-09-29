package com.example.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskflow.dto.TaskStatusCount;
import com.example.taskflow.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务 Mapper。
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /** 按状态统计某项目的任务数量，用于项目统计缓存 */
    @Select("SELECT status, COUNT(*) AS count FROM task WHERE project_id = #{projectId} GROUP BY status")
    List<TaskStatusCount> countByStatus(@Param("projectId") Long projectId);
}
