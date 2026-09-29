package com.example.taskflow.common.result;

/**
 * 响应码枚举。集中管理，避免散落魔法数字。
 * 2xx 成功；4xx 客户端错误；5xx 服务端错误。
 */
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证或 Token 失效"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    USERNAME_EXISTS(40901, "用户名已存在"),
    EMAIL_EXISTS(40902, "邮箱已存在"),
    TEAM_NOT_FOUND(40401, "团队不存在"),
    PROJECT_NOT_FOUND(40402, "项目不存在"),
    USER_NOT_FOUND(40403, "用户不存在"),
    TEAM_MEMBER_NOT_FOUND(40404, "该用户不在团队中"),
    PROJECT_MEMBER_NOT_FOUND(40405, "该用户不在项目中"),
    NOT_TEAM_MEMBER(40301, "你不是该团队成员"),
    NOT_PROJECT_MEMBER(40302, "你不是该项目成员"),
    NO_PERMISSION(40303, "无权限执行此操作"),
    ALREADY_TEAM_MEMBER(40903, "用户已是团队成员"),
    ALREADY_PROJECT_MEMBER(40904, "用户已是项目成员"),
    TASK_NOT_FOUND(40406, "任务不存在"),
    ILLEGAL_STATUS_TRANSITION(40001, "非法的状态流转"),
    ASSIGNEE_NOT_IN_PROJECT(40002, "负责人不在该项目中"),
    COMMENT_NOT_FOUND(40407, "评论不存在"),
    NOTIFICATION_NOT_FOUND(40408, "通知不存在"),
    REFRESH_TOKEN_INVALID(40101, "刷新令牌无效或已过期"),
    TOO_MANY_REQUESTS(42900, "请求过于频繁，请稍后再试"),
    ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
