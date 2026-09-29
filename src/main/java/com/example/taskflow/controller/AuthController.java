package com.example.taskflow.controller;

import com.example.taskflow.common.result.Result;
import com.example.taskflow.dto.LoginRequest;
import com.example.taskflow.dto.LogoutRequest;
import com.example.taskflow.dto.RefreshTokenRequest;
import com.example.taskflow.dto.RegisterRequest;
import com.example.taskflow.service.UserService;
import com.example.taskflow.vo.LoginVO;
import com.example.taskflow.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册、登录、刷新、登出。
 * Controller 只做三件事：接收请求、调用 Service、返回 Result，不写业务逻辑。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** POST /api/auth/register 用户注册 */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /** POST /api/auth/login 用户登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /** POST /api/auth/refresh 用 refresh token 换新 token */
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(userService.refresh(request));
    }

    /** POST /api/auth/logout 登出：失效 refresh token + 拉黑 access token */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) LogoutRequest request,
                               HttpServletRequest httpRequest) {
        String refreshToken = request == null ? null : request.getRefreshToken();
        userService.logout(refreshToken, resolveBearer(httpRequest));
        return Result.success();
    }

    /** 从 "Authorization: Bearer xxx" 里取出 token，无则返回 null */
    private String resolveBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
