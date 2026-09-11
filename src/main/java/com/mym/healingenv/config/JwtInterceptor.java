package com.mym.healingenv.config;

import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.dto.UserDTO;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.service.IUserService;
import com.mym.healingenv.utils.JwtUtils;
import com.mym.healingenv.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws IOException {
        // 放行 options 预检测
        if ("OPTIONS".equals(request.getMethod())){
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            sendError(response, 401, "未登录");
            return false;
        }
        String token = authHeader.substring(7).trim();
        try {
            var decodedJWT = jwtUtils.verify(token);
            Long userId = decodedJWT.getClaim("userId").asLong();
            if (userId == null) {
                sendError(response, 401, "token无效或已过期");
                return false;
            }
            User user = userService.getById(userId);
            if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                sendError(response, 401, "账号不存在或已禁用");
                return false;
            }
            UserDTO userDTO = new UserDTO();
            userDTO.setUserId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setRole(user.getRole());
            UserHolder.saveUser(userDTO);
            if (!hasRequiredRole(handler)) {
                UserHolder.clear();
                sendError(response, 403, "无权访问");
                return false;
            }
            return true;
        } catch (Exception e) {
            sendError(response, 401, "token无效或已过期");
            return false;
        }
    }

    /**
     * 请求结束后清理ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.clear();
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
    }

    private boolean hasRequiredRole(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireRole requiredRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requiredRole == null) {
            requiredRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requiredRole == null) {
            return true;
        }
        UserRole currentRole = UserRole.fromCode(UserHolder.getRole());
        return currentRole != null && Arrays.asList(requiredRole.value()).contains(currentRole);
    }
}
