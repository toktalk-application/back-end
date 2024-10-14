package com.springboot.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.repository.MemberRepository;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.reservation.repository.ReservationRepository;
import com.springboot.reservation.entity.Reservation;
import com.springboot.member.entity.Member;
import com.springboot.counselor.entity.Counselor;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationService {
    private final MemberRepository memberRepository;
    private final CounselorRepository counselorRepository;
    private final ReservationRepository reservationRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final CounselorService counselorService;

    public FirebaseNotificationService(MemberRepository memberRepository,
                                       CounselorRepository counselorRepository,
                                       ReservationRepository reservationRepository, FirebaseMessaging firebaseMessaging, CounselorService counselorService) {
        this.memberRepository = memberRepository;
        this.counselorRepository = counselorRepository;
        this.reservationRepository = reservationRepository;
        this.firebaseMessaging = firebaseMessaging;
        this.counselorService = counselorService;
    }

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

    public boolean sendReservationNotification(Long reservationId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.RESERVATION_NOT_FOUND));
            Counselor counselor = counselorService.findCounselor(reservation.getCounselorId());

            String fcmToken = counselor.getFcmToken();
            if (fcmToken == null || fcmToken.isEmpty()) {
                return false;
            }

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle("새로운 상담 예약")
                            .setBody("새로운 상담이 예약되었습니다. 확인해 주세요.")
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            return false;
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

    // 상담 예약 알림
//    public void sendReservationNotification(Long reservationId) {
//        Reservation reservation = getReservation(reservationId);
//        Counselor counselor = getCounselor(reservation.getCounselorId());
//
//        sendNotification(counselor.getFcmToken(), "새로운 상담 예약", "새로운 상담이 예약되었습니다. 확인해 주세요.");
//    }

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