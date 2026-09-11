package com.mym.healingenv.config;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mym.healingenv.common.RequireRole;
import com.mym.healingenv.common.UserRole;
import com.mym.healingenv.entity.User;
import com.mym.healingenv.service.IUserService;
import com.mym.healingenv.utils.JwtUtils;
import com.mym.healingenv.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private IUserService userService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private JwtInterceptor interceptor;

    @AfterEach
    void clearUserHolder() {
        UserHolder.clear();
    }

    @Test
    void missingAuthorizationHeaderReturnsUnauthorized() throws Exception {
        StringWriter body = new StringWriter();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        verify(response).setStatus(401);
        assertTrue(body.toString().contains("未登录"));
    }

    @Test
    void roleAnnotationRejectsUnauthorizedRole() throws Exception {
        StringWriter body = new StringWriter();
        DecodedJWT decodedJWT = org.mockito.Mockito.mock(DecodedJWT.class);
        Claim claim = org.mockito.Mockito.mock(Claim.class);
        User user = new User();
        user.setId(7L);
        user.setUsername("evaluator");
        user.setRole(UserRole.EVALUATOR.getCode());
        user.setStatus(1);

        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.verify("valid-token")).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("userId")).thenReturn(claim);
        when(claim.asLong()).thenReturn(7L);
        when(userService.getById(7L)).thenReturn(user);
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        Method method = TestController.class.getMethod("adminOnly");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean allowed = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(allowed);
        verify(response).setStatus(403);
        assertEquals(null, UserHolder.getUserId());
    }

    static class TestController {
        @RequireRole(UserRole.ADMIN)
        public void adminOnly() {
        }
    }
}
