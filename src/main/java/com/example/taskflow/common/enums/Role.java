package com.example.taskflow.common.enums;

/**
 * 成员角色枚举。团队和项目共用。
 * 权限大小：OWNER > ADMIN > MEMBER。
 */
public enum Role {

    OWNER,
    ADMIN,
    MEMBER;

    /** 是否有成员管理权限（增删成员、改角色） */
    public boolean canManageMembers() {
        return this == OWNER || this == ADMIN;
    }
}
