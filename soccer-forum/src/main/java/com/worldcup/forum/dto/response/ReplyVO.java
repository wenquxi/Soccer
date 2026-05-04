/** 世界杯论坛 - 回复视图 */
package com.worldcup.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 回复返回
 */
@Data
@AllArgsConstructor
public class ReplyVO {
    private Long id;
    private Long postId;
    private String nickname;
    private String content;
    private String createdAt;
}
