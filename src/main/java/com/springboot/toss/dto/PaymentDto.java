package com.springboot.toss.dto;

import com.springboot.toss.entity.Payment;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PaymentDto {
    @NonNull
    private Payment.PayType payType; // 결제 타입

    @NonNull
    private Long amount; // 가격 정보

    @NonNull
    private String orderName; // 주문명

    private String yourSuccessUrl; // 성공 시 리다이렉트 될 URL
    private String yourFailUrl; // 실패 시 리다이렉트 될 URL
}

