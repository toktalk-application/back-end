package com.springboot.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.auth.userdetails.MemberDetailsService;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.service.CounselorService;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.utils.CredentialUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

// 이 코드는 securityConfiguration의 CustomFilterConfigurer에서 등록됨
public class MemberAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MemberService memberService;
    private final CounselorService counselorService;

    public MemberAuthenticationSuccessHandler(MemberService memberService, CounselorService counselorService) {
        this.memberService = memberService;
        this.counselorService = counselorService;
    }

    // 로그인 인증 성공했을 때 실행
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // CustomAuthentication 객체로 강제 캐스팅해서 보내기
        CustomAuthenticationToken customAuth = (CustomAuthenticationToken) authentication;

        LoginDto.UserType usertype = customAuth.getUserType();
        long identifier = -1;
        switch (usertype){
            case MEMBER:
                Member member = memberService.findMember(authentication.getName());
                identifier = member.getMemberId();
                break;
            case COUNSELOR:
                Counselor counselor = counselorService.findCounselor(authentication.getName());
                identifier = counselor.getCounselorId();
        }

        // 응답에 넣어줄 데이터
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId`", customAuth.getName());
        responseBody.put("authorities", customAuth.getAuthorities());
        responseBody.put("usertype", customAuth.getUserType()); // 추가된 정보
        responseBody.put("identifier", identifier);

        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Map을 JSON으로 변환하여 응답 본문에 작성
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}