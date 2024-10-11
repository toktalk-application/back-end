package com.springboot.toss.dto;

import com.springboot.toss.entity.Payment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PaymentResDto {
    private String payType; // 결제 타입
    private Long amount; // 가격 정보
    private String orderName; // 주문명
    private String orderId; // 주문 Id
    private String userId; // 고객 이메일
    private String nickName; // 고객 이름
    private String successUrl; // 성공 시 리다이렉트 될 URL
    private String failUrl; // 실패 시 리다이렉트 될 URL

    private String failReason; // 실패 이유
    private boolean cancelYN; // 취소 YN
    private String cancelReason; // 취소 이유
    private String createdAt; // 결제가 이루어진 시간
}
