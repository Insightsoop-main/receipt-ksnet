package io.allink.tcp.ksnet.receipt.registration.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class BearerTokenInterceptor implements HandlerInterceptor {

    @Value("${ksnet.registration.bearer-token}")
    private String expectedToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("X-Allink-Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("인증 헤더 없음 - IP={}", request.getRemoteAddr());
            sendUnauthorized(response, "4010", "인증 오류 - 접근 권한 없음");
            return false;
        }

        String token = authHeader.substring(7);
        if (!expectedToken.equals(token)) {
            log.warn("유효하지 않은 토큰 - IP={}", request.getRemoteAddr());
            sendUnauthorized(response, "4012", "유효하지 않은 토큰");
            return false;
        }

        return true;
    }

    private void sendUnauthorized(HttpServletResponse response, String errorCode, String errorMessage) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            String.format("{\"resultCode\":\"NOT_OK\",\"errorCode\":\"%s\",\"errorMessage\":\"%s\"}",
                errorCode, errorMessage)
        );
    }
}
