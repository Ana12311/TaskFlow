package com.example.taskflow.vo;

import com.example.taskflow.entity.TaskComment;
import com.example.taskflow.entity.User;

import java.time.LocalDateTime;

/**
 * 任务评论响应对象，带评论人昵称。
 */
public class TaskCommentVO {

    private Long id;
    private Long taskId;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;

    public static TaskCommentVO from(TaskComment comment, User user) {
        TaskCommentVO vo = new TaskCommentVO();
        vo.setId(comment.getId());
        vo.setTaskId(comment.getTaskId());
        vo.setUserId(comment.getUserId());
        vo.setNickname(user == null ? null : user.getNickname());
        vo.setContent(comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
