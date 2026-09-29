package com.example.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskflow.entity.TaskLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务操作日志 Mapper。
 */
@Mapper
public interface TaskLogMapper extends BaseMapper<TaskLog> {
}
