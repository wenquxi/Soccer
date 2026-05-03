package com.worldcup.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回复实体
 */
@Data
@TableName("reply")
public class Reply {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;            // 所属帖子ID
    private String nickname;        // 昵称
    private String content;         // 内容（上限800字）
    private String ip;              // 回复IP

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableLogic
    private Integer isDeleted;      // 软删除标识
}
