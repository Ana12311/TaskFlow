package com.example.taskflow.controller;

import com.example.taskflow.common.result.Result;
import com.example.taskflow.dto.CommentCreateRequest;
import com.example.taskflow.service.TaskCommentService;
import com.example.taskflow.vo.PageResult;
import com.example.taskflow.vo.TaskCommentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务评论接口。Controller 只接参数调 Service，权限判断在 Service。
 */
@RestController
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    /** 发表评论 */
    @PostMapping("/api/tasks/{taskId}/comments")
    public Result<TaskCommentVO> createComment(@PathVariable Long taskId,
                                               @Valid @RequestBody CommentCreateRequest request) {
        return Result.success(taskCommentService.createComment(taskId, request));
    }

    /** 查询任务评论列表（分页） */
    @GetMapping("/api/tasks/{taskId}/comments")
    public Result<PageResult<TaskCommentVO>> listComments(@PathVariable Long taskId,
                                                          @RequestParam(defaultValue = "1") long pageNum,
                                                          @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(taskCommentService.listComments(taskId, pageNum, pageSize));
    }

    /** 删除自己的评论 */
    @DeleteMapping("/api/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        taskCommentService.deleteComment(commentId);
        return Result.success();
    }
}
