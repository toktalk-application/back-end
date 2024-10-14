package com.springboot.firebase.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
// Serializable Java에서 제공하는 인터페이스로, 객체의 직렬화(Serialization)를 가능하게 함
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String notificationId;
    private Long counselorId;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private boolean isRead;
}