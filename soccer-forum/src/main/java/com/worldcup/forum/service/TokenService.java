/** 世界杯论坛 - Token 管理服务 */
package com.worldcup.forum.service;

import com.worldcup.forum.aspect.Loggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 管理服务（纯 Redis Session），登录时生成 UUID 存入 Redis，登出或过期则删除
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_SEC = 86400; // 24 小时

    private final StringRedisTemplate redisTemplate;

    public TokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建 session，返回 UUID token
     */
    @Loggable("创建会话")
    public String createSession(String username, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_PREFIX + token;
        String value = username + ":" + role;
        try {
            redisTemplate.opsForValue().set(key, value, TOKEN_EXPIRE_SEC, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("写入 Redis 会话失败", e);
            throw new IllegalStateException("会话服务暂不可用，请稍后重试");
        }
        return token;
    }

    /**
     * 校验 token 是否有效（纯 Redis 查询）
     * @return username:role 或 null
     */
    @Loggable("校验令牌")
    public String validate(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            return redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        } catch (Exception e) {
            log.error("校验 Redis 会话失败", e);
            throw new IllegalStateException("认证服务暂不可用，请稍后重试");
        }
    }

    /**
     * 撤销 session（登出）
     */
    @Loggable("撤销会话")
    public void revoke(String token) {
        if (token == null || token.isEmpty()) return;
        try {
            redisTemplate.delete(TOKEN_PREFIX + token);
        } catch (Exception e) {
            log.warn("撤销 Redis 会话失败（已忽略）: {}", e.getMessage());
        }
    }

    /**
     * 从 "username:role" 中提取角色
     */
    public static String extractRole(String value) {
        if (value == null) return null;
        int idx = value.lastIndexOf(':');
        return idx >= 0 ? value.substring(idx + 1) : null;
    }

    /**
     * 从 "username:role" 中提取用户名
     */
    public static String extractUsername(String value) {
        if (value == null) return null;
        int idx = value.lastIndexOf(':');
        return idx >= 0 ? value.substring(0, idx) : value;
    }
}
