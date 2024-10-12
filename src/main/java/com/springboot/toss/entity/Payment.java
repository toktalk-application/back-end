package com.springboot.toss.entity;

import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_payment_member", columnList = "member"),
        @Index(name = "idx_payment_paymentKey", columnList = "paymentKey"),
})
public class Payment{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private long paymentId;

    @Column(nullable = false, name = "pay_type")
    @Enumerated(EnumType.STRING)
    private PayType paymentType;

    @Column(nullable = false, name = "pay_amount")
    private int amount;

    @Column(nullable = false, name = "order_id")
    private String orderId;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "member")
    private Member member;

    @Column
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    private PayStatus payStatus = PayStatus.PENDING;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Getter
    public enum PayType{
        TOSS_PAY("토스페이"),
        SAMSUNG_PAY("삼성페이"),
        NAVER_PAY("네이버페이"),
        KAKAO_PAY("카카오페이"),
        CARD_PAY("카드결제"),
        NORMAL("일반 결제");

        private final String description;

        PayType(String description) {
            this.description = description;
        }
    }

    @Getter
    public enum PayStatus{
        PENDING("결제중"),
        COMPLETED("결제완료"),
        REFUNDED("환불완료");

        private final String description;

        PayStatus(String description) {
            this.description = description;
        }
    }

}
