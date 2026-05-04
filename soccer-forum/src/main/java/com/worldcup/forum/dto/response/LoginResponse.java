/** 世界杯论坛 - 登录响应 */
package com.worldcup.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录返回（统一用于用户和管理员）
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String nickname;
    private String role;
}
