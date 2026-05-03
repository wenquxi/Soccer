package com.worldcup.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.worldcup.forum.dto.request.LoginRequest;
import com.worldcup.forum.dto.request.RegisterRequest;
import com.worldcup.forum.dto.response.LoginResponse;
import com.worldcup.forum.entity.User;
import com.worldcup.forum.mapper.UserMapper;
import com.worldcup.forum.utils.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务：注册、登录、登出
 */
@Service
public class UserService {

    private static final String ROLE_USER = "user";

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, JwtUtils jwtUtils, TokenService tokenService) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.tokenService = tokenService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
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
     * 用户登录，返回 token + 用户信息
     */
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 生成 JWT 并存入 Redis
        String token = jwtUtils.generateToken(user.getUsername());
        tokenService.save(token, user.getUsername(), ROLE_USER);

        return new LoginResponse(token, user.getUsername(), user.getNickname(), ROLE_USER);
    }

    /**
     * 用户登出，撤销 token
     */
    public void logout(String token) {
        tokenService.revoke(token);
    }
}
