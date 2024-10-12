package com.springboot.toss.controller;

import com.springboot.auth.dto.LoginDto;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.response.SingleResponseDto;
import com.springboot.toss.config.TossPaymentConfig;
import com.springboot.toss.dto.PaymentDto;
import com.springboot.toss.dto.PaymentResDto;
import com.springboot.toss.entity.Payment;
import com.springboot.toss.mapper.PaymentMapper;
import com.springboot.toss.service.PaymentService;
import com.springboot.utils.CredentialUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/toss")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final TossPaymentConfig tossPaymentConfig;
    private final PaymentMapper paymentMapper;

    @PostMapping
    public ResponseEntity requestTossPayment(
            Authentication authentication,
            @RequestBody @Valid PaymentDto paymentDto
    ) {
        if(!CredentialUtil.getUserType(authentication).equals(LoginDto.UserType.MEMBER)) {
            throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);
        }

        // 사용자 ID 추출
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));

        Payment payment = paymentMapper.paymentDtoToEntity(paymentDto);
        Payment savedPayment = paymentService.requestTossPayment(payment, memberId);
        PaymentResDto paymentResDto = paymentMapper.entityToPaymentResDto(savedPayment);

        // successUrl과 failUrl 설정
//        paymentResDto.setSuccessUrl(paymentDto.getYourSuccessUrl() != null ?
//                paymentDto.getYourSuccessUrl() : tossPaymentConfig.getSuccessUrl());
//        paymentResDto.setFailUrl(paymentDto.getYourFailUrl() != null ?
//                paymentDto.getYourFailUrl() : tossPaymentConfig.getFailUrl());

        return ResponseEntity.ok(new SingleResponseDto<>(paymentResDto));
    }

    @GetMapping("/success")
    public ResponseEntity<SingleResponseDto<PaymentResDto>> paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount) {
        PaymentResDto paymentResDto = paymentService.verifyPayment(paymentKey, orderId, amount);
        return ResponseEntity.ok(new SingleResponseDto<>(paymentResDto));
    }

    @GetMapping("/fail")
    public ResponseEntity<SingleResponseDto<PaymentResDto>> paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId) {
        PaymentResDto paymentResDto = paymentService.failPayment(code, message, orderId);
        return ResponseEntity.ok(new SingleResponseDto<>(paymentResDto));
    }
}
