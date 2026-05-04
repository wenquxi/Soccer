/** 世界杯论坛 - 管理员控制器 */
package com.worldcup.forum.controller;

import com.worldcup.forum.common.Result;
import com.worldcup.forum.dto.request.AdminLoginRequest;
import com.worldcup.forum.dto.response.LoginResponse;
import com.worldcup.forum.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success(adminService.login(request.getUsername(), request.getPassword()));
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未提供有效的 Authorization 头");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("token 不能为空");
        }
        adminService.logout(token);
        return Result.success();
    }
}
