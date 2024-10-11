package com.springboot.firebase.controller;

import com.springboot.firebase.dto.FcmSendDto;
import com.springboot.firebase.service.FirebaseNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/fcm")
public class FcmController {
    private final FirebaseNotificationService fcmService;

    public FcmController(FirebaseNotificationService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/reservation-notification")
    public ResponseEntity<String> sendReservationNotification(@RequestParam Long reservationId) {
        fcmService.sendReservationNotification(reservationId);
        return ResponseEntity.ok("Reservation notification sent successfully");
    }

    @PostMapping("/reservation-reminder")
    public ResponseEntity<String> sendReservationReminder(@RequestParam Long reservationId) {
        fcmService.sendReservationReminder(reservationId);
        return ResponseEntity.ok("Reservation reminder sent successfully");
    }

    @PostMapping("/cancellation-notification")
    public ResponseEntity<String> sendCancellationNotification(
            @RequestParam Long reservationId,
            @RequestParam boolean cancelledByMember) {
        fcmService.sendCancellationNotification(reservationId, cancelledByMember);
        return ResponseEntity.ok("Cancellation notification sent successfully");
    }

    @PostMapping("/new-message-notification")
    public ResponseEntity<String> sendNewMessageNotification(
            @RequestParam Long recipientId,
            @RequestParam boolean isCounselor) {
        fcmService.sendNewMessageNotification(recipientId, isCounselor);
        return ResponseEntity.ok("New message notification sent successfully");
    }

    @PostMapping("/review-request-notification")
    public ResponseEntity<String> sendReviewRequestNotification(
            @RequestParam Long memberId,
            @RequestParam Long reservationId) {
        fcmService.sendReviewRequestNotification(memberId, reservationId);
        return ResponseEntity.ok("Review request notification sent successfully");
    }

    // 필요한 경우 일반적인 FCM 메시지 전송을 위한 엔드포인트
    @PostMapping("/send")
    public ResponseEntity<String> sendFcmMessage(@RequestBody FcmSendDto fcmSendDto) {
        // 이 메소드는 FirebaseNotificationService에 직접적으로 대응되는 메소드가 없으므로,
        // 필요하다면 서비스에 sendMessage 메소드를 public으로 변경하고 호출해야 합니다.
        // fcmService.sendMessage(fcmSendDto);
        return ResponseEntity.ok("FCM message sent successfully");
    }
}