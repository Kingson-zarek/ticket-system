package com.hnty.ticketsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器，校验请求头中的 JWT
 *
 * @author Crown
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // 静态资源、CORS 预检不是 Controller 方法，放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeError(response, 401, "未登录，请先登录");
            return false;
        }

        try {
            // "Bearer " 占 7 个字符
            String token = header.substring(7);
            Claims claims = jwtUtil.parse(token);
            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);
            UserContext.set(new CurrentUser(userId, role));
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            writeError(response, 401, "登录已过期，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程会被复用，不清会读到上一个用户的信息
        UserContext.clear();
    }

    /**
     * 拦截器无法直接返回 Result，手动写 JSON 到响应体
     */
    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, message)));
    }
}
