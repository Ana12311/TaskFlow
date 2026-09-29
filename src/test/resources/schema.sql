-- 测试库建表脚本。由 spring.sql.init 在每次测试启动时执行，IF NOT EXISTS 保证幂等。
-- 与 db/init.sql 保持一致，只是去掉了 CREATE DATABASE / USE（测试直接连 taskflow_test 库）。

CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名，登录用，唯一',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱，唯一',
    `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
    `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

CREATE TABLE IF NOT EXISTS `team` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '团队ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '团队名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '团队描述',
    `owner_id`    BIGINT       NOT NULL COMMENT '创建者用户ID（OWNER）',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '团队表';

CREATE TABLE IF NOT EXISTS `team_member` (
    `id`        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_id`   BIGINT      NOT NULL COMMENT '团队ID',
    `user_id`   BIGINT      NOT NULL COMMENT '用户ID',
    `role`      VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色：OWNER/ADMIN/MEMBER',
    `joined_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_user` (`team_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '团队成员表';

CREATE TABLE IF NOT EXISTS `project` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    `team_id`     BIGINT       NOT NULL COMMENT '所属团队ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '项目名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '项目描述',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1进行中 0归档',
    `created_by`  BIGINT       NOT NULL COMMENT '创建者用户ID',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_team_id` (`team_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目表';

CREATE TABLE IF NOT EXISTS `project_member` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `project_id` BIGINT      NOT NULL COMMENT '项目ID',
    `user_id`    BIGINT      NOT NULL COMMENT '用户ID',
    `role`       VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色：OWNER/ADMIN/MEMBER',
    `joined_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目成员表';

CREATE TABLE IF NOT EXISTS `task` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `project_id`  BIGINT       NOT NULL COMMENT '所属项目ID',
    `title`       VARCHAR(100) NOT NULL COMMENT '任务标题',
    `description` TEXT         DEFAULT NULL COMMENT '任务描述',
    `creator_id`  BIGINT       NOT NULL COMMENT '创建者用户ID',
    `assignee_id` BIGINT       DEFAULT NULL COMMENT '负责人用户ID，可空',
    `priority`    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：LOW/MEDIUM/HIGH/URGENT',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'TODO' COMMENT '状态：TODO/IN_PROGRESS/DONE/CANCELLED',
    `due_date`    DATE         DEFAULT NULL COMMENT '截止日期',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_assignee_id` (`assignee_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表';

CREATE TABLE IF NOT EXISTS `task_comment` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `task_id`    BIGINT   NOT NULL COMMENT '任务ID',
    `user_id`    BIGINT   NOT NULL COMMENT '评论人用户ID',
    `content`    TEXT     NOT NULL COMMENT '评论内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务评论表';

CREATE TABLE IF NOT EXISTS `task_log` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `task_id`        BIGINT       NOT NULL COMMENT '任务ID',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人用户ID',
    `operation_type` VARCHAR(30)  NOT NULL COMMENT '操作类型',
    `before_value`   VARCHAR(255) DEFAULT NULL COMMENT '变更前值',
    `after_value`    VARCHAR(255) DEFAULT NULL COMMENT '变更后值',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务操作日志表';

CREATE TABLE IF NOT EXISTS `notification` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`    BIGINT       NOT NULL COMMENT '接收者用户ID',
    `type`       VARCHAR(30)  NOT NULL COMMENT '通知类型，见 NotificationType 枚举',
    `title`      VARCHAR(100) DEFAULT NULL COMMENT '通知标题',
    `content`    VARCHAR(255) DEFAULT NULL COMMENT '通知内容',
    `related_id` BIGINT       DEFAULT NULL COMMENT '关联对象ID（任务/团队/项目，看 type）',
    `is_read`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '站内通知表';
