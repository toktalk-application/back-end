package com.springboot.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.springboot.auth.redis.RedisRepositoryConfig;
import com.springboot.firebase.data.*;

import java.util.ArrayList;
import java.util.stream.Collectors;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.repository.MemberRepository;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.reservation.repository.ReservationRepository;
import com.springboot.reservation.entity.Reservation;
import com.springboot.member.entity.Member;
import com.springboot.counselor.entity.Counselor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.management.remote.NotificationResult;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationService {
    private final MemberRepository memberRepository;
    private final CounselorRepository counselorRepository;
    private final ReservationRepository reservationRepository;
    private final CounselorService counselorService;
    private final RedisTemplate<String, Object> redisTemplate;

    public void sendChatRoomCreationNotification(long memberId, long roomId) {
        try {
            log.info("Sending chat room creation notification: memberId={}, roomId={}", memberId, roomId);

            Member member = getMember(memberId);
            String fcmToken = member.getFcmToken();

            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("FCM token not found: userId={}", memberId);
                return;
            }

            if (roomId <= 0) {
                throw new IllegalArgumentException("Invalid roomId: " + roomId);
            }

            com.springboot.firebase.data.Notification notification = createNotification(
                    memberId, roomId, 0,"새로운 채팅방", "새로운 채팅방이 생성되었습니다.",
                    com.springboot.firebase.data.Notification.NotificationType.CHAT
            );

            saveNotificationToRedis(member.getUserId(), notification);
            sendFcmMessage(fcmToken, notification.getTitle(), notification.getBody());

            log.info("Chat room creation notification sent successfully: memberId={}, roomId={}", memberId, roomId);
        } catch (Exception e) {
            log.error("Failed to send chat room creation notification: memberId={}, roomId={}", memberId, roomId, e);
        }
    }

    public boolean sendReservationNotification(Long reservationId) {
        try {
            log.info("Sending reservation notification: reservationId={}", reservationId);

            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.RESERVATION_NOT_FOUND));

            Counselor counselor = counselorService.findCounselor(reservation.getCounselorId());
            String fcmToken = counselor.getFcmToken();

            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("FCM token not found: counselorId={}", counselor.getCounselorId());
                return false;
            }

            com.springboot.firebase.data.Notification notification = createNotification(
                    counselor.getCounselorId(), 0, reservation.getReservationId(),
                    "새로운 상담 예약", "새로운 상담이 예약되었습니다. 확인해 주세요.",
                    com.springboot.firebase.data.Notification.NotificationType.RESERVATION
            );

            saveNotificationToRedis(counselor.getUserId(), notification);
            String response = sendFcmMessage(fcmToken, notification.getTitle(), notification.getBody());

            boolean result = response != null && !response.isEmpty();
            log.info("Reservation notification sent: success={}, reservationId={}", result, reservationId);
            return result;
        } catch (Exception e) {
            log.error("Failed to send reservation notification: reservationId={}", reservationId, e);
            return false;
        }
    }

    public boolean deleteNotification(String userId, String notificationId) {
        String key = "notifications:" + userId;
        try {
            List<Object> objectList = redisTemplate.opsForList().range(key, 0, -1);
            if (objectList != null) {
                for (Object obj : objectList) {
                    if (obj instanceof com.springboot.firebase.data.Notification) {
                        com.springboot.firebase.data.Notification notification = (com.springboot.firebase.data.Notification) obj;
                        if (notification.getNotificationId().equals(notificationId)) {
                            long removed = redisTemplate.opsForList().remove(key, 1, notification);
                            log.info("Notification deleted: userId={}, notificationId={}, removedCount={}", userId, notificationId, removed);
                            return removed > 0;
                        }
                    }
                }
            }
            log.warn("Notification not found: userId={}, notificationId={}", userId, notificationId);
            return false;
        } catch (Exception e) {
            log.error("Error deleting notification: userId={}, notificationId={}", userId, notificationId, e);
            return false;
        }
    }

    public List<com.springboot.firebase.data.Notification> getNotificationsForUser(String userId) {
        String key = "notifications:" + userId;
        List<Object> notifications = redisTemplate.opsForList().range(key, 0, -1);
        return notifications.stream()
                .filter(obj -> obj instanceof com.springboot.firebase.data.Notification)
                .map(obj -> (com.springboot.firebase.data.Notification) obj)
                .collect(Collectors.toList());
    }

    public void markNotificationAsRead(String userId, String notificationId) {
        String key = "notifications:" + userId;
        List<Object> notifications = redisTemplate.opsForList().range(key, 0, -1);
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i) instanceof com.springboot.firebase.data.Notification) {
                com.springboot.firebase.data.Notification notification = (com.springboot.firebase.data.Notification) notifications.get(i);
                if (notification.getNotificationId().equals(notificationId)) {
                    notification.setRead(true);
                    redisTemplate.opsForList().set(key, i, notification);
                    log.info("Notification marked as read: userId={}, notificationId={}", userId, notificationId);
                    return;
                }
            }
        }
        log.warn("Notification not found for marking as read: userId={}, notificationId={}", userId, notificationId);
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
    }

    private com.springboot.firebase.data.Notification createNotification(long userId,long roomId, long reservationId, String title, String body, com.springboot.firebase.data.Notification.NotificationType type) {
        return new com.springboot.firebase.data.Notification(
                UUID.randomUUID().toString(),
                userId,       // counselorId or userId
                roomId,            // roomId는 필요 없을 때 0으로 설정
                reservationId,
                title,
                body,
                LocalDateTime.now(),
                false,
                type
        );
    }

    private void saveNotificationToRedis(String userId, com.springboot.firebase.data.Notification notification) {
        String key = "notifications:" + userId;
        try {
            redisTemplate.opsForList().leftPush(key, notification);
            log.info("Notification saved to Redis: userId={}, notificationId={}", userId, notification.getNotificationId());
        } catch (Exception e) {
            log.error("Failed to save notification to Redis: userId={}, notificationId={}", userId, notification.getNotificationId(), e);
        }
    }

    private String sendFcmMessage(String fcmToken, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }

    // 예약된 상담 찾기
    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    // 상담사 찾기
    private Counselor getCounselor(Long counselorId) {
        return counselorRepository.findById(counselorId)
                .orElseThrow(() -> new RuntimeException("Counselor not found"));
    }

    // 사용자 찾기
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    // 상담 시작전 알림
    public void sendReservationReminder(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        Member member = reservation.getMember();
        Counselor counselor = getCounselor(reservation.getCounselorId());

        sendNotification(member.getFcmToken(), "상담 시작 알림", "10분 후 상담이 시작됩니다.");
        sendNotification(counselor.getFcmToken(), "상담 시작 알림", "10분 후 상담이 시작됩니다.");
    }

    // 상담취소 알림
    public void sendCancellationNotification(Long reservationId, boolean cancelledByMember) {
        Reservation reservation = getReservation(reservationId);
        String recipientToken;
        if (cancelledByMember) {
            Counselor counselor = getCounselor(reservation.getCounselorId());
            recipientToken = counselor.getFcmToken();
        } else {
            recipientToken = reservation.getMember().getFcmToken();
        }

        String message = cancelledByMember ?
                "사용자가 상담을 취소했습니다." :
                "상담사가 상담을 취소했습니다. 죄송합니다.";

        sendNotification(recipientToken, "상담 취소 알림", message);
    }

    // 새로운 메세지 알림
    public void sendNewMessageNotification(Long recipientId, boolean isCounselor) {
        String recipientToken;
        if (isCounselor) {
            Counselor counselor = getCounselor(recipientId);
            recipientToken = counselor.getFcmToken();
        } else {
            Member member = getMember(recipientId);
            recipientToken = member.getFcmToken();
        }

        sendNotification(recipientToken, "새 메시지 도착", "새로운 메시지가 도착했습니다.");
    }

    // 사용자한테 상담 후기 작성요청 알림
    public void sendReviewRequestNotification(Long memberId, Long reservationId) {
        Member member = getMember(memberId);
        sendNotification(member.getFcmToken(), "상담 후기 작성 요청", "최근 진행한 상담에 대한 후기를 작성해 주세요.");
    }

    // 알림
    public void sendNotification(String token, String title, String body) {
        try {
            sendFcmMessage(token, title, body);
        } catch (FirebaseMessagingException e) {
            System.err.println("Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}