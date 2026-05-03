package com.worldcup.forum.service;

import com.worldcup.forum.dto.response.LoginResponse;
import com.worldcup.forum.utils.JwtUtils;
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
    private final JwtUtils jwtUtils;
    private final TokenService tokenService;

    public AdminService(@Value("${admin.username}") String adminUsername,
                        @Value("${admin.password}") String adminPassword,
                        JwtUtils jwtUtils, TokenService tokenService) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.jwtUtils = jwtUtils;
        this.tokenService = tokenService;
    }

    /**
     * 管理员登录，生成 token 并存入 Redis
     */
    public LoginResponse login(String username, String password) {
        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtUtils.generateToken(username);
        tokenService.save(token, username, ROLE_ADMIN);
        return new LoginResponse(token, username, "管理员", ROLE_ADMIN);
    }

    /**
     * 管理员登出，撤销 token
     */
    public void logout(String token) {
        tokenService.revoke(token);
    }
}
