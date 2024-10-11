package com.springboot.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.counselor.entity.Counselor;
import com.springboot.auth.dto.LoginDto;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// 로그인 요청을 받는 클래스
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenizer jwtTokenizer) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
    }
    // (1) 로그인 요청이 여기로 들어옴
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

        // (2) UsernamePasswordAuthenticationToken을 상속하는 객체 - 이걸로 Authentication 생성
        CustomAuthenticationToken authenticationToken =
                new CustomAuthenticationToken(loginDto.getUserId(), loginDto.getPassword(), loginDto.getUserType());

        // (3) AuthenticationManager 에게 Authentication 전달
        // 4번부터는 AuthenticationProvider에서 계속
        return authenticationManager.authenticate(authenticationToken);
    }

    // (11) AuthenticationManager가 인증된 Authentication을 UsernamePasswordAuthenticationFilter에 전달 (인증 성공 시 코드 흐름이 여기로 옴)
    // 이 뒤부터는 강의자료 흐름 그림에 누락돼 있는데 successfulAuthentication이 실제로 액세스 토큰을 발행하는 과정이 여기 있음
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication)throws ServletException, IOException {


        Object principal = authentication.getPrincipal();
        String accessToken;
        String refreshToken;

        if (principal instanceof Member) {
            Member member = (Member) principal;
            accessToken = delegateAccessToken(member); // 멤버용 액세스 토큰 발행
            refreshToken = delegateRefreshToken(member, accessToken);
        } else if (principal instanceof Counselor) { // Counselor 처리 추가
            Counselor counselor = (Counselor) principal;
            accessToken = delegateAccessToken(counselor); // 상담사용 액세스 토큰 발행
            refreshToken = delegateRefreshToken(counselor, accessToken);
        } else {
            throw new IllegalArgumentException("Unknown principal type");
        }

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);

        // 기본 successHandler 대신 SecurityConfiguration에서 등록한 MemberAuthenticationSuccessHandler 가 사용됨
        this.getSuccessHandler().onAuthenticationSuccess(request,response,authentication);
    }

    // 실제 액세스 토큰을 발행하는 과정
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
    protected String delegateRefreshToken(Member member, String accessToken){
        String subject = member.getUserId();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject,expiration,base64EncodedSecretKey, accessToken);
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
    protected String delegateRefreshToken(Counselor counselor, String accessToken){
        String subject = counselor.getUserId();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject,expiration,base64EncodedSecretKey, accessToken);
        return refreshToken;
    }
}