package com.springboot.toss.entity;

import com.springboot.member.entity.Member;
import lombok.*;

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
    private PayType payType;

    @Column(nullable = false, name = "pay_amount")
    private long amount;

    @Column(nullable = false, name = "pay_name")
    private String orderName;

    @Column(nullable = false, name = "order_id")
    private String orderId;

    private boolean paySuccessYN;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "member")
    private Member member;

    @Column
    private String paymentKey;

    @Column
    private String failReason;

    @Column
    private String cancelReason;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Getter
    public enum PayType{
        TOSS_PAY("토스페이"),
        SAMSUNG_PAY("삼성페이"),
        NAVER_PAY("네이버페이"),
        KAKAO_PAY("카카오페이");

        private final String description;

        PayType(String description) {
            this.description = description;
        }
    }

}
