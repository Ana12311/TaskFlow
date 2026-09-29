package com.example.taskflow.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 添加团队成员请求体。
 * role 可选，不传默认 MEMBER；传了必须是 OWNER/ADMIN/MEMBER（Service 里校验）。
 */
public class TeamMemberAddRequest {

    @NotNull(message = "userId 不能为空")
    private Long userId;

    /** 可选角色，默认 MEMBER */
    private String role;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
