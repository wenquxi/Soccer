/** 世界杯论坛 - 帖子实体 */
package com.worldcup.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体
 */
@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String nickname;        // 昵称
    private String content;         // 内容
    private String ip;              // 发布IP
    private Integer replyCount;     // 回复数

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableLogic
    private Integer isDeleted;      // 软删除标识
}
