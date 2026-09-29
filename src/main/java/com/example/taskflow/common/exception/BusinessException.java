package com.example.taskflow.common.exception;

import com.example.taskflow.common.result.ResultCode;

/**
 * 业务异常。Service 里遇到业务不满足（如用户名重复）就主动抛它，
 * 由 GlobalExceptionHandler 统一转成 Result 返回给前端。
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
