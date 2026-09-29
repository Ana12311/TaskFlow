package com.example.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.taskflow.dto.LoginRequest;
import com.example.taskflow.dto.RefreshTokenRequest;
import com.example.taskflow.dto.RegisterRequest;
import com.example.taskflow.dto.UserUpdateRequest;
import com.example.taskflow.entity.User;
import com.example.taskflow.vo.LoginVO;
import com.example.taskflow.vo.UserVO;

/**
 * 用户业务接口。继承 IService<User> 获得 save/getById 等通用 CRUD。
 */
public interface UserService extends IService<User> {

    /** 注册：校验唯一性、BCrypt 加密、入库，返回用户信息 */
    UserVO register(RegisterRequest request);

    /** 登录：认证、生成 JWT，返回 token + 用户信息 */
    LoginVO login(LoginRequest request);

    /** 刷新：用 refresh token 换新的 access + refresh token */
    LoginVO refresh(RefreshTokenRequest request);

    /** 登出：失效 refresh token + 拉黑 access token */
    void logout(String refreshToken, String accessToken);

    /** 按用户名查用户，查不到返回 null */
    User getByUsername(String username);

    /** 修改当前用户资料（昵称/头像），并删除用户缓存 */
    UserVO updateProfile(UserUpdateRequest request);
}
