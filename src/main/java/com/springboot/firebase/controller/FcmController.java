package com.springboot.firebase.controller;

import com.springboot.chat.service.ChatRoomService;
import com.springboot.firebase.dto.FcmSendDto;
import com.springboot.firebase.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/fcm")
public class FcmController {
    private final NotificationService fcmService;

    public FcmController(NotificationService fcmService, ChatRoomService chatRoomService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/reservation-notification")
    public ResponseEntity<String> sendReservationNotification(@RequestParam Long reservationId) {
        fcmService.sendReservationNotification(reservationId);
        return ResponseEntity.ok("Reservation notification sent successfully");
    }

    // 필요한 경우 일반적인 FCM 메시지 전송을 위한 엔드포인트
    @PostMapping("/send")
    public ResponseEntity<String> sendFcmMessage(@RequestBody FcmSendDto fcmSendDto) {
        // 이 메소드는 FirebaseNotificationService에 직접적으로 대응되는 메소드가 없으므로,
        // 필요하다면 서비스에 sendMessage 메소드를 public으로 변경하고 호출해야 합니다.
        // fcmService.sendMessage(fcmSendDto);
        return ResponseEntity.ok("FCM message sent successfully");
    }

    @GetMapping
    public ResponseEntity<List<com.springboot.firebase.data.Notification>> getUserNotifications(Authentication authentication) {
        String userId = getUserIdFromAuthentication(authentication);
        List<com.springboot.firebase.data.Notification> notifications = fcmService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(
            Authentication authentication,
            @PathVariable String notificationId) {
        String userId = getUserIdFromAuthentication(authentication);
        fcmService.markNotificationAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(Authentication authentication, @PathVariable("notificationId") String notificationId) {
        String userId = getUserIdFromAuthentication(authentication);
        boolean deleted = fcmService.deleteNotification(userId, notificationId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/chat/{notificationId}")
    public ResponseEntity<?> deleteChatNotification(Authentication authentication, @PathVariable("notificationId") String notificationId) {
        String userId = getUserIdFromAuthentication(authentication);
        boolean deleted = fcmService.deleteNotification(userId, notificationId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication");
        }
        return authentication.getPrincipal().toString();
    }
}