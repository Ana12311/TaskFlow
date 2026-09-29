package com.example.taskflow.dto;

/**
 * 登出请求体。refreshToken 可选：不传则只拉黑 access token。
 */
public class LogoutRequest {

    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
