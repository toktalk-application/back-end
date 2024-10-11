package com.springboot.auth.handler;

import com.springboot.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 인증 실패 시 처리를 담당하는 AuthenticationEntryPoint 구현체
@Slf4j
@Component
public class MemberAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Exception exception = (Exception) request.getAttribute("exception");
        ErrorResponse.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid authentication");

        logExceptionMessage(authException, exception);
    }

    private void logExceptionMessage(AuthenticationException authException,
                                     Exception e) {
        String message = e != null ? e.getMessage() : authException.getMessage();
        log.warn("Unauthorized error : {}", message);
    }
}