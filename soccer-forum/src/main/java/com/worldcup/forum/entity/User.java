/** 世界杯论坛 - 用户实体 */
package com.worldcup.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;        // 用户名（唯一）
    private String password;        // BCrypt 加密后的密码
    private String nickname;        // 显示昵称

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 注册时间

    @TableLogic
    private Integer isDeleted;      // 软删除标识
}
