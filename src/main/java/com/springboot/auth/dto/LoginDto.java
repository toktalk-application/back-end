package com.springboot.auth.dto;

import lombok.Getter;

@Getter
public class LoginDto {
    private String userId;
    private String password;
    private UserType userType;

    public enum UserType{
        MEMBER,
        COUNSELOR
    }
}
