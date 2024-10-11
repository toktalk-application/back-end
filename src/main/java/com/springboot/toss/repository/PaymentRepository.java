package com.springboot.toss.repository;

import com.springboot.toss.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

//    Optional<Payment> findByPaymentKeyAndUserId(String paymentKey, String userId);
//
//    Slice<Payment> findAllByUserId(String userId, Pageable pageable);
}
