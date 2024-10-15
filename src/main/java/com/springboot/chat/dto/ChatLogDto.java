package com.springboot.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class ChatLogDto {
    @Getter
    @Setter
    public static class Post {
        private long roomId;
        private String message;
        private String sender;
        @JsonCreator
        public Post(
                @JsonProperty("sender") String sender,
                @JsonProperty("message") String message,
                @JsonProperty("roomId") Long roomId) {
            this.sender = sender;
            this.message = message;
            this.roomId = roomId;
        }
    }
    @Builder
    @Getter
    @Setter
    public static class Response {
        private long logId;
        private String sender;
        private String message;
//        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
//        private String createdAt;
    }
}
