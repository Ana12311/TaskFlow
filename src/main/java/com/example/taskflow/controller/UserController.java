package com.example.taskflow.controller;

import com.example.taskflow.common.result.Result;
import com.example.taskflow.dto.UserUpdateRequest;
import com.example.taskflow.security.LoginUser;
import com.example.taskflow.service.UserService;
import com.example.taskflow.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。
 * GET /api/users/me 返回当前登录用户（信息已在 SecurityContext，无需查库）。
 * PUT /api/users/me 修改当前用户资料，改完删除用户缓存。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(UserVO.from(loginUser.getUser()));
    }

    @PutMapping("/me")
    public Result<UserVO> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.updateProfile(request));
    }
}
