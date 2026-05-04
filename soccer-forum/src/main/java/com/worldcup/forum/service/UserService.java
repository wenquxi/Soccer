/** 世界杯论坛 - 用户服务 */
package com.worldcup.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.worldcup.forum.aspect.Loggable;
import com.worldcup.forum.dto.request.LoginRequest;
import com.worldcup.forum.dto.request.RegisterRequest;
import com.worldcup.forum.dto.response.LoginResponse;
import com.worldcup.forum.entity.User;
import com.worldcup.forum.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务：注册、登录、登出
 */
@Service
public class UserService {

    private static final String ROLE_USER = "user";

    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, TokenService tokenService) {
        this.userMapper = userMapper;
        this.tokenService = tokenService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户注册
     */
    @Loggable("用户注册")
    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        userMapper.insert(user);
    }

    /**
     * 用户登录，创建 Redis session，返回 token
     */
    @Loggable("用户登录")
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = tokenService.createSession(user.getUsername(), ROLE_USER);
        return new LoginResponse(token, user.getUsername(), user.getNickname(), ROLE_USER);
    }

    /**
     * 用户登出
     */
    @Loggable("用户登出")
    public void logout(String token) {
        tokenService.revoke(token);
    }
}
