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
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        userService.logout(token);
        return Result.success();
    }
}
