package com.springboot.toss.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentVerifyRequest {
    private long amount;
    private String orderId;
}

