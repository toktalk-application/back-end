package com.springboot.toss.mapper;

import com.springboot.toss.dto.PaymentDto;
import com.springboot.toss.dto.PaymentFailDto;
import com.springboot.toss.dto.PaymentResDto;
import com.springboot.toss.dto.TossPaymentSuccessDto;
import com.springboot.toss.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "orderId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "paySuccessYN", constant = "false")
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "paymentKey", ignore = true)
    @Mapping(target = "failReason", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    Payment paymentDtoToEntity(PaymentDto paymentDto);

    @Mapping(target = "payType", source = "payType.description")
    @Mapping(target = "userId", source = "member.userId")
    @Mapping(target = "nickName", source = "member.nickname")
    @Mapping(target = "createdAt", expression = "java(payment.getCreatedAt().toString())")
    @Mapping(target = "successUrl", ignore = true)
    @Mapping(target = "failUrl", ignore = true)
    PaymentResDto entityToPaymentResDto(Payment payment);

    PaymentFailDto entityToPaymentFailDto(Payment payment);

    PaymentResDto tossPaymentSuccessDtoToPaymentResDto(TossPaymentSuccessDto tossPaymentSuccessDto);
}
