package com.example.taskflow.common.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 固定窗口限流器：用 Redis 计数器实现，适合登录/注册等防暴力破解场景。
 * Redis 不可用时“放行”（fail-open），保证限流组件故障不影响正常业务。
 */
@Component
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试占用一次配额（固定窗口：窗口内计数，超限拒绝）。
     *
     * @return true 表示允许本次请求，false 表示超过限制、应拒绝
     */
    public boolean tryAcquire(String key, int limit, Duration window) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            // 第一次计数时设置过期时间，形成“窗口”；之后每次命中不清零
            if (count != null && count == 1L) {
                redisTemplate.expire(key, window);
            }
            return count == null || count <= limit;
        } catch (Exception e) {
            // Redis 故障时放行，避免限流组件误伤正常请求
            return true;
        }
    }
}
