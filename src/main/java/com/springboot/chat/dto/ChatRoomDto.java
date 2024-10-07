package com.springboot.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomDto {
    @Builder
    @Getter
    @Setter
    public static class Response {
        private long roomId;
        private long memberId;
        private long counselorId;
        private String roomStatus;
        private LocalDateTime createdAt;
        private List<ChatLogDto.Response> chatLogs;
    }
}
