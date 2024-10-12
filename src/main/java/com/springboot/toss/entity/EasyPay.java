package com.springboot.toss.entity;

import lombok.Data;

@Data
class EasyPay {
    private String provider;
    private int amount;
    private int discountAmount;
}
