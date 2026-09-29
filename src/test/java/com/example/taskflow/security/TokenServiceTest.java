package com.example.taskflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TokenService 纯单元测试：不启动 Spring 容器，用 Mockito + 内存 Map 模拟 Redis 的 set/get/delete/hasKey，
 * 验证 refresh token 白名单、access token 黑名单的读写逻辑。
 */
class TokenServiceTest {

    /** 构造一个以内存 Map 为后端的 TokenService，模拟 Redis 行为 */
    private TokenService newService(Map<String, String> store) {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        doAnswer(inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(ops).set(anyString(), anyString(), any(Duration.class));
        when(ops.get(anyString())).thenAnswer(inv -> store.get(inv.getArgument(0)));

        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenReturn(ops);
        doAnswer(inv -> store.remove(inv.getArgument(0))).when(redis).delete(anyString());
        when(redis.hasKey(anyString())).thenAnswer(inv -> store.containsKey(inv.getArgument(0)));
        return new TokenService(redis);
    }

    @Test
    void storeAndGetRefreshToken_roundTrip() {
        Map<String, String> store = new HashMap<>();
        TokenService service = newService(store);

        service.storeRefreshToken("rt-1", 42L, 60000);

        assertEquals(42L, service.getUserIdByRefreshToken("rt-1"));
    }

    @Test
    void getRefreshToken_missing_returnsNull() {
        TokenService service = newService(new HashMap<>());

        assertNull(service.getUserIdByRefreshToken("not-exist"));
    }

    @Test
    void deleteRefreshToken_removes() {
        Map<String, String> store = new HashMap<>();
        TokenService service = newService(store);
        service.storeRefreshToken("rt-1", 42L, 60000);

        service.deleteRefreshToken("rt-1");

        assertNull(service.getUserIdByRefreshToken("rt-1"));
    }

    @Test
    void blacklistAndCheck() {
        Map<String, String> store = new HashMap<>();
        TokenService service = newService(store);

        service.blacklist("jti-1", Duration.ofMinutes(1));

        assertTrue(service.isBlacklisted("jti-1"));
        assertFalse(service.isBlacklisted("jti-2"));
    }
}
