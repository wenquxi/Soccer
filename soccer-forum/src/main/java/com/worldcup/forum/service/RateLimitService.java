/** 世界杯论坛 - IP 限流服务 */
package com.worldcup.forum.service;

import com.worldcup.forum.aspect.Loggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * IP 限流服务（基于 Redis）
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private static final int MAX_REQUESTS = 5;       // 最大请求次数
    private static final int TIME_WINDOW = 60;        // 时间窗口（秒）
    private static final String KEY_PREFIX = "rate:";

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查 IP 是否超过限流阈值
     * @return true=允许请求，false=已被限流
     */
    @Loggable("限流检查")
    public boolean tryAcquire(String ip) {
        String key = KEY_PREFIX + (ip != null ? ip : "unknown");
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) return true;

            if (count == 1) {
                redisTemplate.expire(key, TIME_WINDOW, TimeUnit.SECONDS);
            }

            return count <= MAX_REQUESTS;
        } catch (Exception e) {
            log.warn("限流 Redis 不可用，降级放行: {}", e.getMessage());
            return true;
        }
    }
}
