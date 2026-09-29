package com.example.taskflow.security;

import com.example.taskflow.common.cache.CacheKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Token 存储：refresh token 白名单 + access token 黑名单，都放 Redis。
 * 所有操作 try/catch 兜底：Redis 挂了就降级（黑名单查不到视为未拉黑），绝不让认证链路崩溃。
 */
@Component
public class TokenService {

    private final StringRedisTemplate redisTemplate;

    public TokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 存 refresh token，value 是 userId，过期时间与 refresh token 有效期一致 */
    public void storeRefreshToken(String refreshToken, Long userId, long expirationMillis) {
        try {
            redisTemplate.opsForValue().set(
                    CacheKeys.REFRESH_TOKEN + refreshToken,
                    String.valueOf(userId),
                    Duration.ofMillis(expirationMillis));
        } catch (Exception ignored) {
        }
    }

    /** 查 refresh token 对应的 userId，不存在或已过期返回 null */
    public Long getUserIdByRefreshToken(String refreshToken) {
        try {
            String userId = redisTemplate.opsForValue().get(CacheKeys.REFRESH_TOKEN + refreshToken);
            return userId == null ? null : Long.valueOf(userId);
        } catch (Exception e) {
            return null;
        }
    }

    /** 删除 refresh token（登出 / 刷新轮换时） */
    public void deleteRefreshToken(String refreshToken) {
        try {
            redisTemplate.delete(CacheKeys.REFRESH_TOKEN + refreshToken);
        } catch (Exception ignored) {
        }
    }

    /** access token 加入黑名单，TTL 设为 token 剩余有效期 */
    public void blacklist(String jti, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(CacheKeys.BLACKLIST + jti, "1", ttl);
        } catch (Exception ignored) {
        }
    }

    /** 是否已拉黑。Redis 异常时返回 false（放行），保证可用性 */
    public boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(CacheKeys.BLACKLIST + jti));
        } catch (Exception e) {
            return false;
        }
    }
}
