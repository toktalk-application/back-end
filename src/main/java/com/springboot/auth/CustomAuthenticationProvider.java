package com.springboot.auth;

import com.springboot.auth.userdetails.CounselorDetailsService;
import com.springboot.auth.userdetails.MemberDetailsService;
import com.springboot.auth.dto.LoginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// 커스텀 AuthenticationProvider (SecurityConfig -> SecurityFilterChain 에서 적용해줘야 사용 가능)
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final MemberDetailsService memberDetailsService;
    private final CounselorDetailsService counselorDetailsService; // Assume this exists
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomAuthenticationProvider(@Lazy MemberDetailsService memberDetailsService,
                                        @Lazy CounselorDetailsService counselorDetailsService,
                                        @Lazy PasswordEncoder passwordEncoder) {
        this.memberDetailsService = memberDetailsService;
        this.counselorDetailsService = counselorDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // AuthenticationManager로부터 호출
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userId = authentication.getName();
        String password = authentication.getCredentials().toString();

        LoginDto.UserType userType = ((CustomAuthenticationToken) authentication).getUserType();

        //(4) 인증을 위한 Authentication 전달
        UserDetails userDetails;
        switch (userType) {
            // (5) UserDetailsService를 통해 UserDetails 조회
            case MEMBER:
                // (6) ~ (8) 은 UserDetailsService에서 수행
                userDetails = memberDetailsService.loadUserByUsername(userId);
                break;
            case COUNSELOR:
                userDetails = counselorDetailsService.loadUserByUsername(userId);
                break;
            default:
                throw new AuthenticationException("Invalid user type") {};
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        /*return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());*/
        // (9) UserDetail을 통해 인증된 Authentication 생성
        // (10) 인증된 Authentication을 AuthenticationManager에게 반환
        // 그 후 곧바로 AuthenticationManager에 의해 JwtAuthenticationFilter(UsernamePasswordAuthenticationFilter) 의 successfulAuthentication로 흐름 이동
        return new CustomAuthenticationToken(userDetails, password, userType);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
