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
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userId = authentication.getName();
        String password = authentication.getCredentials().toString();

        LoginDto.UserType userType = ((CustomAuthenticationToken) authentication).getUserType();
        UserDetails userDetails;
        switch (userType) {
            case MEMBER:
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
        return new CustomAuthenticationToken(userDetails, password, userType);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
