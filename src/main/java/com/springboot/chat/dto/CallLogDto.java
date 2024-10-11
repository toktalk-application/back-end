package com.springboot.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class CallLogDto {
    @Builder
    @Getter
    @Setter
    public static class Response {
        private String fileUrl;
        private LocalDateTime createdAt;
    }
}
