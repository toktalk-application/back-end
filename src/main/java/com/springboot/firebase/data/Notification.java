package com.springboot.firebase.data;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
// Serializable Java에서 제공하는 인터페이스로, 객체의 직렬화(Serialization)를 가능하게 함
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String notificationId;
    private Long counselorId;
    private long reservationId;
    private long roomId;
    private String nickName;
    private String counselorName;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private boolean isRead;
    private NotificationType type;

    public Notification(String notificationId, long counselorId, long roomId, long reservationId, String nickName, String counselorName, String title, String body, LocalDateTime createdAt, boolean isRead, NotificationType type) {
        this.notificationId = notificationId;
        this.counselorId = counselorId;
        this.roomId = roomId;
        this.reservationId = reservationId;
        this.nickName = nickName;
        this.counselorName = counselorName;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.type = type;
    }

    public enum NotificationType {
        CHAT, RESERVATION
    }
}