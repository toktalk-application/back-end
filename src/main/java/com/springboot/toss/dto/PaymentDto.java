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
    private Payment.PayType paymentType; // 결제 타입

    @NonNull
    private Long amount; // 가격 정보

    @NonNull
    private String orderId; // 주문명

    @NonNull
    private String paymentKey; // 주문명
}

