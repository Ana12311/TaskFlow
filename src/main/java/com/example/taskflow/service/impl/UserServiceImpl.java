package com.example.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.taskflow.common.cache.CacheKeys;
import com.example.taskflow.common.cache.CacheService;
import com.example.taskflow.common.exception.BusinessException;
import com.example.taskflow.common.result.ResultCode;
import com.example.taskflow.dto.LoginRequest;
import com.example.taskflow.dto.RefreshTokenRequest;
import com.example.taskflow.dto.RegisterRequest;
import com.example.taskflow.dto.UserUpdateRequest;
import com.example.taskflow.entity.User;
import com.example.taskflow.mapper.UserMapper;
import com.example.taskflow.security.JwtTokenProvider;
import com.example.taskflow.security.LoginUser;
import com.example.taskflow.security.TokenService;
import com.example.taskflow.service.UserService;
import com.example.taskflow.util.SecurityUtils;
import com.example.taskflow.vo.LoginVO;
import com.example.taskflow.vo.UserVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 用户业务实现。注册/登录/刷新/登出的核心逻辑都在这里。
 * 继承 ServiceImpl<UserMapper, User> 自动获得 save/getById/count 等 CRUD 方法。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CacheService cacheService;
    private final TokenService tokenService;

    public UserServiceImpl(PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthenticationManager authenticationManager,
                           CacheService cacheService,
                           TokenService tokenService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.cacheService = cacheService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public UserVO register(RegisterRequest request) {
        // 1. 校验用户名、邮箱唯一
        if (getByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        if (count(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())) > 0) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 2. 构建用户，密码用 BCrypt 加密后再存
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(1);

        // 3. 入库，save 后 user.getId() 会被回填自增主键
        save(user);
        return UserVO.from(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        // 交给 AuthenticationManager 认证（内部走 UserDetailsService 查用户 + BCrypt 比对）
        // 用户名不存在或密码错误会抛 AuthenticationException，由全局异常处理转成 401
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // 认证成功，principal 就是 LoginUser，从中取出用户实体
        User user = ((LoginUser) authentication.getPrincipal()).getUser();
        return issueTokens(user);
    }

    @Override
    public LoginVO refresh(RefreshTokenRequest request) {
        // 1. 校验 refresh token：查 Redis 白名单，不存在/过期即拒绝
        Long userId = tokenService.getUserIdByRefreshToken(request.getRefreshToken());
        if (userId == null) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 2. 轮换：旧的 refresh token 作废，发一套新的（防止泄露的 token 被反复使用）
        tokenService.deleteRefreshToken(request.getRefreshToken());
        return issueTokens(user);
    }

    @Override
    public void logout(String refreshToken, String accessToken) {
        // 1. 删除 refresh token，之后无法再用它换新
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenService.deleteRefreshToken(refreshToken);
        }
        // 2. access token 加入黑名单，TTL = 剩余有效期，到期自动清理
        if (accessToken != null && !accessToken.isBlank() && jwtTokenProvider.validateToken(accessToken)) {
            tokenService.blacklist(jwtTokenProvider.getJti(accessToken),
                    Duration.ofMillis(jwtTokenProvider.getRemainingMillis(accessToken)));
        }
    }

    @Override
    @Transactional
    public UserVO updateProfile(UserUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        updateById(user);
        // 改资料后失效用户缓存，下次读取重新查库
        cacheService.delete(CacheKeys.USER_INFO + userId);
        return UserVO.from(user);
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    /** 生成 access + refresh token，把 refresh token 存 Redis，组装 LoginVO */
    private LoginVO issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken();
        // refresh token 存 Redis（白名单），登出删除、刷新轮换
        tokenService.storeRefreshToken(refreshToken, user.getId(), jwtTokenProvider.getRefreshExpiration());

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setUser(UserVO.from(user));
        return vo;
    }
}
