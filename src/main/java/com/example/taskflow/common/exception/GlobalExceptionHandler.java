package com.example.taskflow.common.exception;

import com.example.taskflow.common.result.Result;
import com.example.taskflow.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理。@RestControllerAdvice 拦截所有 Controller 抛出的异常，
 * 统一转成 Result 返回，绝不把堆栈直接丢给前端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：直接返回它携带的 code 和 message */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** @Valid 参数校验失败：取第一条错误提示返回 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ResultCode.BAD_REQUEST.getMessage();
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 请求体无法解析（JSON 语法错 / 编码错）：返回 400 而不是 500 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /** 认证失败（登录）：统一返回"用户名或密码错误"，不泄露具体是哪个错 */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        String message = (e instanceof DisabledException) ? "账号已被禁用" : "用户名或密码错误";
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
    }

    /** 兜底：其余未知异常返回 500，务必打日志方便排查 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未捕获异常", e);
        return Result.error(ResultCode.ERROR);
    }
}
