-- 世界杯论坛数据库初始化脚本
-- 使用前请先创建数据库: CREATE DATABASE worldcup_forum CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 帖子表
CREATE TABLE IF NOT EXISTS `post` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nickname`    VARCHAR(50)  NOT NULL                COMMENT '昵称',
    `content`     TEXT         NOT NULL                COMMENT '内容',
    `ip`          VARCHAR(45)  DEFAULT NULL             COMMENT '发布IP',
    `reply_count` INT          NOT NULL DEFAULT 0      COMMENT '回复数',
    `created_at`  DATETIME     NOT NULL                COMMENT '创建时间',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '软删除标识 0=正常 1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 回复表
CREATE TABLE IF NOT EXISTS `reply` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`    BIGINT       NOT NULL                COMMENT '所属帖子ID',
    `nickname`   VARCHAR(50)  NOT NULL                COMMENT '昵称',
    `content`    VARCHAR(500) NOT NULL                COMMENT '内容',
    `ip`         VARCHAR(45)  DEFAULT NULL             COMMENT '回复IP',
    `created_at` DATETIME     NOT NULL                COMMENT '创建时间',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0      COMMENT '软删除标识 0=正常 1=删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(30)  NOT NULL                COMMENT '用户名（唯一）',
    `password`   VARCHAR(100) NOT NULL                COMMENT 'BCrypt加密密码',
    `nickname`   VARCHAR(50)  NOT NULL                COMMENT '显示昵称',
    `created_at` DATETIME     NOT NULL                COMMENT '注册时间',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0      COMMENT '软删除标识 0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
