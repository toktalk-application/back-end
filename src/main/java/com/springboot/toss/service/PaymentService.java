package com.springboot.toss.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.toss.config.TossPaymentConfig;
import com.springboot.toss.dto.PaymentFailDto;
import com.springboot.toss.dto.PaymentResDto;
import com.springboot.toss.dto.PaymentVerifyRequest;
import com.springboot.toss.dto.TossPaymentSuccessDto;
import com.springboot.toss.entity.Payment;
import com.springboot.toss.mapper.PaymentMapper;
import com.springboot.toss.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final RestTemplate restTemplate;
    private final TossPaymentConfig tossPaymentConfig;
    private final PaymentMapper paymentMapper;

    public Payment requestTossPayment(Payment payment, long memberId) {
        Member member = memberService.findMember(memberId);

        if (payment.getAmount() < 1) {
            throw new BusinessLogicException(ExceptionCode.INVALID_PAYMENT_AMOUNT);
        }

        payment.setMember(member);
        return paymentRepository.save(payment);
    }

    public PaymentResDto verifyPayment(String paymentKey, String orderId, long amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((tossPaymentConfig.getTestSecretKey() + ":").getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        PaymentVerifyRequest verifyRequest = new PaymentVerifyRequest(amount, orderId);

        HttpEntity<PaymentVerifyRequest> request = new HttpEntity<>(verifyRequest, headers);

        ResponseEntity<TossPaymentSuccessDto> response = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/" + paymentKey,
                request,
                TossPaymentSuccessDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.PAYMENT_NOT_FOUND));

            payment.setPaySuccessYN(true);
            payment.setPaymentKey(paymentKey);
            paymentRepository.save(payment);

            return paymentMapper.tossPaymentSuccessDtoToPaymentResDto(response.getBody());
        } else {
            throw new BusinessLogicException(ExceptionCode.PAYMENT_FAILED);
        }
    }

    public PaymentResDto failPayment(String code, String message, String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.PAYMENT_NOT_FOUND));

        payment.setPaySuccessYN(false);
        payment.setFailReason(message);
        paymentRepository.save(payment);

        return paymentMapper.entityToPaymentResDto(payment);
    }
}