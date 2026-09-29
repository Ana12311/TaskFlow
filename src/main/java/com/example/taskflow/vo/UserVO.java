package com.example.taskflow.vo;

import com.example.taskflow.entity.User;

import java.time.LocalDateTime;

/**
 * 用户信息响应对象。刻意不含 password 字段，绝不把密码返回给前端。
 * 提供 from(User) 静态方法做 Entity -> VO 转换，转换逻辑集中在一处。
 */
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private LocalDateTime createdAt;

    /** Entity -> VO 转换，只挑需要暴露的字段 */
    public static UserVO from(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
