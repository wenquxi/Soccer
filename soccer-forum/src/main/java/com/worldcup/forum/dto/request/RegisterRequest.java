package com.worldcup.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度3-30字")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50字")
    private String nickname;
}
