package com.example.taskflow.common.cache;

/**
 * 缓存 key 前缀集中管理，避免散落魔法字符串。
 */
public final class CacheKeys {

    /** 用户信息缓存，key = user:info:{userId} */
    public static final String USER_INFO = "user:info:";

    /** 项目任务统计缓存，key = project:stats:{projectId} */
    public static final String PROJECT_STATS = "project:stats:";

    /** refresh token 白名单，key = auth:refresh:{refreshToken}，value = userId */
    public static final String REFRESH_TOKEN = "auth:refresh:";

    /** access token 黑名单，key = auth:blacklist:{jti} */
    public static final String BLACKLIST = "auth:blacklist:";

    private CacheKeys() {
    }
}
