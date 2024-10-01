package com.springboot.auth;

import com.springboot.dto.LoginDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final LoginDto.UserType userType;

    public CustomAuthenticationToken(Object principal, Object credentials, LoginDto.UserType userType) {
        super(principal, credentials);
        this.userType = userType;
    }

    public LoginDto.UserType getUserType() {
        return userType;
    }
}
