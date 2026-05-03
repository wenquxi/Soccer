package com.worldcup.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发布帖子请求
 */
@Data
public class PostCreateRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50字")
    private String nickname;

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容最长10000字")
    private String content;
}
