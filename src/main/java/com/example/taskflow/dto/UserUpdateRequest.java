package com.example.taskflow.dto;

import jakarta.validation.constraints.Size;

/**
 * 修改当前用户资料请求体。字段可空，只更新非空字段（部分更新）。
 */
public class UserUpdateRequest {

    @Size(max = 50, message = "昵称最长 50 字符")
    private String nickname;

    @Size(max = 255, message = "头像地址最长 255 字符")
    private String avatar;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
