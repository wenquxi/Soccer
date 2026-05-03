package com.worldcup.forum.service;

import com.worldcup.forum.utils.JwtUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 管理服务（基于 Redis），支持登录态校验和主动登出
 */
@Service
public class TokenService {

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_MS = 86400000; // 24h，与 JWT 过期时间一致

    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;

    public TokenService(StringRedisTemplate redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 保存 token 到 Redis，以 jwt 为 key，username:role 为 value
     */
    public void save(String token, String username, String role) {
        String key = TOKEN_PREFIX + token;
        String value = username + ":" + role;
        redisTemplate.opsForValue().set(key, value, TOKEN_EXPIRE_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 校验 token 是否有效（Redis 中存在且 JWT 解析正常）
     * @return role 或 null
     */
    public String validate(String token) {
        String key = TOKEN_PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return null; // 已过期或已登出

        // 验证 JWT 签名
        String username = jwtUtils.parseToken(token);
        if (username == null) {
            redisTemplate.delete(key); // JWT 无效则清理
            return null;
        }
        return value; // username:role
    }

    /**
     * 撤销 token（登出）
     */
    public void revoke(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    /**
     * 从 tokenValue 中提取角色
     */
    public static String extractRole(String tokenValue) {
        if (tokenValue == null) return null;
        int idx = tokenValue.lastIndexOf(':');
        return idx >= 0 ? tokenValue.substring(idx + 1) : null;
    }

    /**
     * 从 tokenValue 中提取用户名
     */
    public static String extractUsername(String tokenValue) {
        if (tokenValue == null) return null;
        int idx = tokenValue.lastIndexOf(':');
        return idx >= 0 ? tokenValue.substring(0, idx) : tokenValue;
    }
}
