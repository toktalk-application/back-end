package com.springboot.auth.filter;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.auth.utils.CustomAuthorityUtils;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            Map<String,Object> claims = verifyJws(request);
            setAuthenticationToContext(claims);
        } catch (ExpiredJwtException ee){
            request.setAttribute("exception",ee);
        }catch (Exception e){
            request.setAttribute("exception",e);
        }
        filterChain.doFilter(request,response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        String authorization = request.getHeader("Authorization");
        return authorization == null || !authorization.startsWith("Bearer");
    }
    private Map<String,Object> verifyJws(HttpServletRequest request){
        String jws = request.getHeader("Authorization")
                .replace("Bearer ", "");
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        Map<String,Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
        return claims;
    }

    // 어떤 요청을 받았을 때 넣어 준 액세스 토큰을 Authentication 객체로 변환하는 코드
    private void setAuthenticationToContext(Map<String,Object> claims){
        String username = (String) claims.get("username");
        List<GrantedAuthority> authorityList = authorityUtils.createAuthorities((List)claims.get("roles"));

        LoginDto.UserType userType;
        Map<String, String> credentials = new HashMap<>();
        switch ((String) claims.get("usertype")){
            case "MEMBER":
                userType = LoginDto.UserType.MEMBER;
                credentials.put("memberId", String.valueOf(claims.get("memberId")));
                break;
            case "COUNSELOR":
                userType = LoginDto.UserType.COUNSELOR;
                credentials.put("counselorId", String.valueOf(claims.get("counselorId")));
                break;
            default:
                userType = null;
        }
        credentials.put("userId", (String) claims.get("userId"));
        /*Authentication authentication = new UsernamePasswordAuthenticationToken(username,null,authorityList);*/
        Authentication authentication = new CustomAuthenticationToken(username, credentials, authorityList, userType);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}