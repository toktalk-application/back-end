package com.springboot.testresult.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class TestResultDto {
    @Builder
    @Getter
    @Setter
    public static class Post {
        private long memberId;
        private int score;
    }

    @Builder
    @Getter
    @Setter
    public static class Response {
        private int score;
        private String description;
        private String comment;
        private LocalDateTime createdAt;
    }
}
