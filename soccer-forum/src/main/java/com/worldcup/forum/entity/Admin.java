package com.worldcup.forum.entity;

import lombok.Data;

/**
 * 管理员实体（非数据库表，从配置读取）
 */
@Data
public class Admin {
    private String username;
    private String password;
}
