package com.worldcup.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发布回复请求
 */
@Data
public class ReplyCreateRequest {
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 100, message = "昵称最长50字")
    private String nickname;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 800, message = "回复内容最长800字")
    private String content;
}
