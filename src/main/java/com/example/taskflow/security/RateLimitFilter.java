package com.example.taskflow.security;

import com.example.taskflow.common.cache.RateLimiter;
import com.example.taskflow.common.result.Result;
import com.example.taskflow.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * 认证接口限流：对登录/注册按客户端 IP 做固定窗口限流，防暴力破解。
 * 只拦截 POST /api/auth/login 和 /api/auth/register，其余请求直接放行。
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /** 每分钟最多请求次数 */
    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isAuthEndpoint(request) || rateLimiter.tryAcquire(key(request), MAX_ATTEMPTS, WINDOW)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 超限：返回 429 + 统一响应体
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(ResultCode.TOO_MANY_REQUESTS)));
    }

    private boolean isAuthEndpoint(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri.equals("/api/auth/login") || uri.equals("/api/auth/register");
    }

    private String key(HttpServletRequest request) {
        // 优先取反向代理透传的真实 IP，否则取直连 IP
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return "rate:auth:" + ip;
    }
}
