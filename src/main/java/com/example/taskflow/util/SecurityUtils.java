package com.example.taskflow.util;

import com.example.taskflow.common.exception.BusinessException;
import com.example.taskflow.common.result.ResultCode;
import com.example.taskflow.entity.User;
import com.example.taskflow.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类：从 SecurityContext 拿当前登录用户。
 * JwtAuthenticationFilter 已经把 LoginUser 放进 SecurityContext，这里封装取出逻辑，
 * 避免每个 Service/Controller 重复写强转。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 取当前登录用户实体，未认证则抛 401 */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUser();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    /** 取当前登录用户 id */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
