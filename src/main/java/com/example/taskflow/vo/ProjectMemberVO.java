package com.example.taskflow.vo;

import com.example.taskflow.entity.ProjectMember;
import com.example.taskflow.entity.User;

import java.time.LocalDateTime;

/**
 * 项目成员信息响应对象：成员关系 + 用户基础信息。
 */
public class ProjectMemberVO {

    private Long userId;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime joinedAt;

    public static ProjectMemberVO of(ProjectMember member, User user) {
        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setUserId(member.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(member.getRole());
        vo.setJoinedAt(member.getJoinedAt());
        return vo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
