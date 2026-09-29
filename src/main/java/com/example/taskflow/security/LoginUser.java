package com.example.taskflow.security;

import com.example.taskflow.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 自定义 UserDetails：把业务 User 实体包一层交给 Spring Security。
 * 好处：登录后 SecurityContext 里能拿到完整用户信息，Controller 直接取 id/昵称，不用再查库。
 */
public class LoginUser implements UserDetails {

    private final User user;

    public LoginUser(User user) {
        this.user = user;
    }

    /** 取出真正的用户实体，Controller 层用 */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 本项目暂无角色权限，返回空集合
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // status = 0 表示账号被禁用
        return user.getStatus() == null || user.getStatus() == 1;
    }
}
