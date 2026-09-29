package com.example.taskflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类：负责生成/解析 access token，以及生成 refresh token。
 * HS256 对称加密：同一个密钥既能签名也能校验，所以密钥绝不能泄露。
 * 两种 token 分工：
 * - access token：短时效（默认 30 分钟），每次请求携带，subject 存 userId，jti 存唯一 ID 用于登出黑名单
 * - refresh token：长时效（默认 7 天），不参与 JWT 签名，只是一个随机串，服务端存 Redis 白名单
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    /** 从 application.yml 读密钥和两个过期时间，启动时初始化 */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-expiration}") long accessExpiration,
                            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /** 生成 access token：subject 存 userId，jti 存唯一 ID 供黑名单比对 */
    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpiration);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 生成 refresh token：不透明随机串，服务端存 Redis，不参与 JWT 签名 */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** refresh token 的有效期（毫秒），存 Redis 时用 */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    /** 解析 Token 并校验签名与过期时间，返回载荷；非法则抛异常 */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验 Token 是否有效：能解析且没过期即有效 */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 取 jti（token 唯一 ID），登出黑名单用 */
    public String getJti(String token) {
        return parseToken(token).getId();
    }

    /** 剩余有效毫秒数，用于给黑名单设置 TTL（token 过期后黑名单记录自然清理） */
    public long getRemainingMillis(String token) {
        long remaining = parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }
}
