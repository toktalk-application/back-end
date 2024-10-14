package com.springboot.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.springboot.auth.redis.RedisRepositoryConfig;
import com.springboot.firebase.data.*;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.management.remote.NotificationResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirebaseNotificationService {
    private final MemberRepository memberRepository;
    private final CounselorRepository counselorRepository;
    private final ReservationRepository reservationRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final CounselorService counselorService;
    private final RedisTemplate<String, Object> redisTemplate;

    public void sendMessage(String token, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setToken(token)  // 여기에 토큰을 설정합니다.
                .build();

        String response = firebaseMessaging.send(message);
        System.out.println("Successfully sent message: " + response);
    }

    // 이 메서드는 너무 많은걸 하고 있음. 추상화해서 분리필요.
    // 예약Id를 찾아서 상담주체인 상담사의 Id를 찾아와 예약이 생기면 알림전송.
    public boolean sendReservationNotification(Long reservationId) {
        try {
            System.out.println("알림 전송 시작: 예약 ID = " + reservationId);

            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.RESERVATION_NOT_FOUND));
            System.out.println("예약 정보 조회 성공");

            Counselor counselor = counselorService.findCounselor(reservation.getCounselorId());
            System.out.println("상담사 정보 조회 성공: 상담사 ID = " + counselor.getCounselorId());

            String fcmToken = counselor.getFcmToken();
            if (fcmToken == null || fcmToken.isEmpty()) {
                System.out.println("FCM 토큰 없음: 상담사 ID = " + counselor.getCounselorId());
                return false;
            }
            System.out.println("FCM 토큰 확인 완료");

            // 알림 객체 생성
            com.springboot.firebase.data.Notification notification = new com.springboot.firebase.data.Notification(
                    UUID.randomUUID().toString(),
                    counselor.getCounselorId(),
                    reservation.getReservationId(),
                    "새로운 상담 예약",
                    "새로운 상담이 예약되었습니다. 확인해 주세요.",
                    LocalDateTime.now(),
                    false
            );
            System.out.println("알림 객체 생성 완료");

            // Redis에 알림 저장
            String key = "notifications:" + counselor.getUserId(); // 내가 처음에 식별자 id로 가져옴
            try {
                redisTemplate.opsForList().leftPush(key, notification);
                System.out.println("Redis에 알림 저장 성공: key = " + key);
            } catch (Exception e) {
                System.out.println("Redis에 알림 저장 실패: " + e.getMessage());
                // Redis 저장 실패 시에도 FCM 메시지 전송 계속 진행
            }

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getBody())
                            .build())
                    .build();
            System.out.println("FCM 메시지 객체 생성 완료");

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("FCM 메시지 전송 완료: response = " + response);

            boolean result = response != null && !response.isEmpty();
            System.out.println("알림 전송 결과: " + (result ? "성공" : "실패"));
            return result;
        } catch (Exception e) {
            System.out.println("알림 전송 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
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
                            System.out.println("알림 삭제 완료: notificationId = " + notificationId + ", 삭제된 항목 수 = " + removed);
                            return removed > 0;
                        }
                    }
                }
            }
            System.out.println("알림을 찾을 수 없음: userId = " + userId + ", notificationId = " + notificationId);
            return false;
        } catch (Exception e) {
            System.out.println("알림 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<com.springboot.firebase.data.Notification> getNotificationsForUser(String userId) {
        String key = "notifications:" + userId;
        List<Object> notifications = redisTemplate.opsForList().range(key, 0, -1);
        return notifications.stream()
                .map(obj -> (com.springboot.firebase.data.Notification) obj)
                .collect(Collectors.toList());
    }

    public void markNotificationAsRead(String userId, String notificationId) {
        String key = "notifications:" + userId;
        List<Object> notifications = redisTemplate.opsForList().range(key, 0, -1);
        for (int i = 0; i < notifications.size(); i++) {
            com.springboot.firebase.data.Notification notification
                    = (com.springboot.firebase.data.Notification) notifications.get(i);
            if (notification.getNotificationId().equals(notificationId)) {
                notification.setRead(true);
                redisTemplate.opsForList().set(key, i, notification);
                break;
            }
        }
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
            sendMessage(token, title, body);
        } catch (FirebaseMessagingException e) {
            System.err.println("Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}