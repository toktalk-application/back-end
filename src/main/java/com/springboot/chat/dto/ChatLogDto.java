package com.springboot.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class ChatLogDto {
    @Builder
    @Getter
    @Setter
    public static class Post {
        private String message;
        private String sender;
    }
    @Builder
    @Getter
    @Setter
    public static class Response {
        private long logId;
        private String sender;
        private String message;
        private LocalDateTime createdAt;
    }
}
