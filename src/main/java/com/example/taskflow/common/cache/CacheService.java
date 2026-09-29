package com.example.taskflow.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 轻量缓存工具：封装 Redis 的 JSON 读写，统一序列化/反序列化。
 * 缓存异常一律静默跳过，绝不影响主业务。
 */
@Component
public class CacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /** 读缓存，命中返回对象；未命中或反序列化失败返回 null */
    public <T> T get(String key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /** 写缓存，带过期时间；序列化失败静默跳过 */
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException ignored) {
            // 忽略，不影响主流程
        }
    }

    /** 删除缓存 */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
