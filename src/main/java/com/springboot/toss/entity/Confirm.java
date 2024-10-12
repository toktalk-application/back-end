package com.springboot.toss.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Confirm {
    private String paymentKey;
    private String orderId;
    private int amount;
}
