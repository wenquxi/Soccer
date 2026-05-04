/** 世界杯论坛 - 帖子列表视图 */
package com.worldcup.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 帖子列表返回（含回复摘要）
 */
@Data
@AllArgsConstructor
public class PostVO {
    private Long id;
    private String nickname;
    private String content;
    private Integer replyCount;
    private String createdAt;
    private Object latestReplies; // 最新5条回复
}
