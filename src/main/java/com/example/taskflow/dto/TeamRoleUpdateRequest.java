package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改团队成员角色请求体。
 */
public class TeamRoleUpdateRequest {

    @NotBlank(message = "角色不能为空")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
