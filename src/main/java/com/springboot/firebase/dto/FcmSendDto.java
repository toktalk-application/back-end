package com.springboot.firebase.dto;

import lombok.*;

@Getter
@Setter
public class FcmSendDto {
    private String title;
    private String message;
    private String token;
}
