package com.example.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.taskflow.dto.CommentCreateRequest;
import com.example.taskflow.entity.TaskComment;
import com.example.taskflow.vo.PageResult;
import com.example.taskflow.vo.TaskCommentVO;

/**
 * 任务评论业务接口。
 */
public interface TaskCommentService extends IService<TaskComment> {

    /** 发表评论，须是任务所属项目成员 */
    TaskCommentVO createComment(Long taskId, CommentCreateRequest request);

    /** 分页查询任务评论列表，须是项目成员 */
    PageResult<TaskCommentVO> listComments(Long taskId, long pageNum, long pageSize);

    /** 删除自己的评论 */
    void deleteComment(Long commentId);
}
