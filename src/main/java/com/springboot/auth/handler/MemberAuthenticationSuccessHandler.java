package com.springboot.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.auth.CustomAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MemberAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // CustomAuthentication 객체로 강제 캐스팅해서 보내기
        CustomAuthenticationToken customAuth = (CustomAuthenticationToken) authentication;

        // 응답에 넣어줄 데이터
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("username", customAuth.getName());
        responseBody.put("authorities", customAuth.getAuthorities());
        responseBody.put("usertype", customAuth.getUserType()); // 추가된 정보

        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Map을 JSON으로 변환하여 응답 본문에 작성
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}