package com.springboot.toss.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentSuccessDto {
    private String paymentKey;
    private String orderId;
    private Long amount;
}
