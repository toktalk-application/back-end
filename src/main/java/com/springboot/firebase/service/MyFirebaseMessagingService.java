package com.springboot.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.springboot.firebase.dto.FcmSendDto;
import com.springboot.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class MyFirebaseMessagingService {
    private final MemberRepository memberRepository;

    public MyFirebaseMessagingService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 특정 사용자에게 메시지 전송
    public void sendMessage(FcmSendDto request) {
        try {
            // FCM 메시지 빌드
            Message message = Message.builder()
                    .setToken(request.getToken())  // 클라이언트의 FCM 토큰
                    .putData("title", request.getTitle())  // 제목
                    .putData("message", request.getMessage())  // 메시지 내용
                    .build();

            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
