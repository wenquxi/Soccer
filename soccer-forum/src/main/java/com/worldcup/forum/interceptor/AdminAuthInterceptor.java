package com.worldcup.forum.interceptor;

import com.worldcup.forum.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员认证拦截器（校验 Redis 中是否存在有效 token，且角色为 admin）
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_TOKEN = "Authorization";

    private final TokenService tokenService;

    public AdminAuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(HEADER_TOKEN);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或token无效");
            return false;
        }

        String token = authHeader.substring(7);
        String tokenValue = tokenService.validate(token);
        if (tokenValue == null) {
            writeUnauthorized(response, "token已过期或无效");
            return false;
        }

        // 校验角色是否为 admin
        String role = TokenService.extractRole(tokenValue);
        if (!"admin".equals(role)) {
            writeUnauthorized(response, "无管理员权限");
            return false;
        }

        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
