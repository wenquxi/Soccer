/** 世界杯论坛 - 用户控制器 */
package com.worldcup.forum.controller;

import com.worldcup.forum.common.Result;
import com.worldcup.forum.dto.request.LoginRequest;
import com.worldcup.forum.dto.request.RegisterRequest;
import com.worldcup.forum.dto.response.LoginResponse;
import com.worldcup.forum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器：注册、登录、登出
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    /**
     * 用户登录，返回 token
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 用户登出
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
        userService.logout(token);
        return Result.success();
    }
}
