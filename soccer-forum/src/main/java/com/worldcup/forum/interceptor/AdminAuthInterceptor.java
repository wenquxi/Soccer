/** 世界杯论坛 - 管理员认证拦截器 */
package com.worldcup.forum.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcup.forum.common.Result;
import com.worldcup.forum.service.TokenService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员认证拦截器（校验 Redis Session 是否存在且角色为 admin）
 */

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthInterceptor.class);

    private static final String HEADER_TOKEN = "Authorization";

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public AdminAuthInterceptor(TokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @Nullable HttpServletResponse response,
                             @Nullable Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (response == null) {
            return false;
        }

        String authHeader = request.getHeader(HEADER_TOKEN);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(response, 401, Result.unauthorized("未登录或token无效"));
            return false;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            writeJson(response, 401, Result.unauthorized("未登录或token无效"));
            return false;
        }

        final String sessionValue;
        try {
            sessionValue = tokenService.validate(token);
        } catch (IllegalStateException e) {
            log.warn("管理员认证依赖服务异常: {}", e.getMessage());
            writeJson(response, 503, Result.error(503, e.getMessage()));
            return false;
        } catch (Exception e) {
            log.error("管理员认证异常", e);
            writeJson(response, 503, Result.error(503, "认证服务暂不可用，请稍后重试"));
            return false;
        }

        if (sessionValue == null) {
            writeJson(response, 401, Result.unauthorized("token已过期或无效"));
            return false;
        }

        String role = TokenService.extractRole(sessionValue);
        if (!"admin".equals(role)) {
            writeJson(response, 401, Result.unauthorized("无管理员权限"));
            return false;
        }

        return true;
    }

    private void writeJson(HttpServletResponse response, int httpStatus, Result<?> body) {
        try {
            response.setStatus(httpStatus);
            response.setContentType("application/json;charset=utf-8");
            objectMapper.writeValue(response.getWriter(), body);
        } catch (Exception e) {
            log.error("写入认证响应失败", e);
            try {
                response.sendError(httpStatus);
            } catch (Exception ignored) {
                // 最后降级：忽略
            }
        }
    }
}
