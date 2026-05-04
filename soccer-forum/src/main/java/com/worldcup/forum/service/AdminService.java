/** 世界杯论坛 - 管理员服务 */
package com.worldcup.forum.service;

import com.worldcup.forum.aspect.Loggable;
import com.worldcup.forum.dto.response.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 管理员服务
 */
@Service
public class AdminService {

    private static final String ROLE_ADMIN = "admin";

    private final String adminUsername;
    private final String adminPassword;
    private final TokenService tokenService;

    public AdminService(@Value("${admin.username}") String adminUsername,
                        @Value("${admin.password}") String adminPassword,
                        TokenService tokenService) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.tokenService = tokenService;
    }

    /**
     * 管理员登录，创建 Redis session
     */
    @Loggable("管理员登录")
    public LoginResponse login(String username, String password) {
        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = tokenService.createSession(username, ROLE_ADMIN);
        return new LoginResponse(token, username, "管理员", ROLE_ADMIN);
    }

    /**
     * 管理员登出
     */
    @Loggable("管理员登出")
    public void logout(String token) {
        tokenService.revoke(token);
    }
}
