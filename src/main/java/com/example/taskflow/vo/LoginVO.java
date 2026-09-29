package com.example.taskflow.vo;

/**
 * 登录/刷新成功响应：access token + refresh token + 用户信息。
 * access token 短时效（过期用 refresh token 换新），refresh token 登出即失效。
 */
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private UserVO user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
