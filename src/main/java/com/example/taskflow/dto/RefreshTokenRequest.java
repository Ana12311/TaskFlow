package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新 token 请求体。
 */
public class RefreshTokenRequest {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
