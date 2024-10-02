package com.springboot.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.counselor.entity.Counselor;
import com.springboot.auth.dto.LoginDto;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.member.entity.Member;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenizer jwtTokenizer) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
    }
    /*@SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response){
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUserId(), loginDto.getPassword());
        return authenticationManager.authenticate(authenticationToken);
    }*/
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

        CustomAuthenticationToken authenticationToken =
                new CustomAuthenticationToken(loginDto.getUserId(), loginDto.getPassword(), loginDto.getUserType());

        return authenticationManager.authenticate(authenticationToken);
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication)throws ServletException, IOException {
        /*Member member = (Member) authentication.getPrincipal();
        String accessToken = delegateAccessToken(member);
        String refreshToken = delegateRefreshToken(member);*/

        Object principal = authentication.getPrincipal();
        String accessToken;
        String refreshToken;

        if (principal instanceof Member) {
            Member member = (Member) principal;
            accessToken = delegateAccessToken(member);
            refreshToken = delegateRefreshToken(member);
        } else if (principal instanceof Counselor) { // Counselor 처리 추가
            Counselor counselor = (Counselor) principal;
            accessToken = delegateAccessToken(counselor);
            refreshToken = delegateRefreshToken(counselor);
        } else {
            throw new IllegalArgumentException("Unknown principal type");
        }

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);

        this.getSuccessHandler().onAuthenticationSuccess(request,response,authentication);
    }
    protected String delegateAccessToken(Member member){
        Map<String,Object> claims = new HashMap<>();
        claims.put("username", member.getUserId());
        claims.put("roles", member.getRoles());
        claims.put("usertype", "MEMBER");
        claims.put("memberId", member.getMemberId());
        claims.put("userId", member.getUserId());

        String subject = member.getUserId();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String accessToken = jwtTokenizer.generatedAccessToken(claims,subject,expiration,base64EncodedSecretKey);
        return accessToken;
    }
    protected String delegateRefreshToken(Member member){
        String subject = member.getUserId();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject,expiration,base64EncodedSecretKey);
        return refreshToken;
    }
    protected String delegateAccessToken(Counselor counselor){
        Map<String,Object> claims = new HashMap<>();
        claims.put("username", counselor.getUserId());
        claims.put("roles", counselor.getRoles());
        claims.put("usertype", "COUNSELOR");
        claims.put("counselorId", counselor.getCounselorId());
        claims.put("userId", counselor.getUserId());

        String subject = counselor.getUserId();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String accessToken = jwtTokenizer.generatedAccessToken(claims,subject,expiration,base64EncodedSecretKey);
        return accessToken;
    }
    protected String delegateRefreshToken(Counselor counselor){
        String subject = counselor.getUserId();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject,expiration,base64EncodedSecretKey);
        return refreshToken;
    }
}