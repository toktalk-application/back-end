package com.springboot.auth;

import com.springboot.auth.dto.LoginDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final LoginDto.UserType userType;

    public CustomAuthenticationToken(Object principal, Object credentials, LoginDto.UserType userType) {
        super(principal, credentials);
        this.userType = userType;
    }
    public CustomAuthenticationToken(Object principal, Object credentials, List<GrantedAuthority> authorities, LoginDto.UserType userType) {
        super(principal, credentials, authorities);
        this.userType = userType;
    }

    public LoginDto.UserType getUserType() {
        return userType;
    }
}
