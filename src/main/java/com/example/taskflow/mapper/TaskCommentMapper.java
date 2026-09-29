package com.example.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskflow.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务评论 Mapper。
 */
@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {
}
