package com.example.taskflow.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.taskflow.common.cache.CacheKeys;
import com.example.taskflow.common.cache.CacheService;
import com.example.taskflow.entity.User;
import com.example.taskflow.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Spring Security 的"按用户名加载用户"实现。
 * 登录认证时（DaoAuthenticationProvider）会调它拿到用户和密码，再比对 BCrypt。
 * 额外提供 loadById：供 JWT 过滤器按 id 查用户，走 Redis 缓存，避免每个请求查库。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final CacheService cacheService;

    public UserDetailsServiceImpl(UserMapper userMapper, CacheService cacheService) {
        this.userMapper = userMapper;
        this.cacheService = cacheService;
    }

    /** 登录用：按用户名查用户（不走缓存，必须拿到最新密码做 BCrypt 比对） */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new LoginUser(user);
    }

    /** 过滤器用：按 id 查用户，走 Redis 缓存。缓存的是实体，含 BCrypt 密文（非明文），Redis 与 DB 同属内部可信存储 */
    public LoginUser loadById(Long id) {
        String key = CacheKeys.USER_INFO + id;
        User user = cacheService.get(key, User.class);
        if (user == null) {
            user = userMapper.selectById(id);
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在: " + id);
            }
            cacheService.set(key, user, Duration.ofMinutes(30));
        }
        return new LoginUser(user);
    }
}
