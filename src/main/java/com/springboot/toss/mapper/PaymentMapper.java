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
    Payment paymentDtoToEntity(PaymentDto paymentDto);

    PaymentResDto entityToPaymentResDto(Payment payment);

    PaymentFailDto entityToPaymentFailDto(Payment payment);

    PaymentResDto tossPaymentSuccessDtoToPaymentResDto(TossPaymentSuccessDto tossPaymentSuccessDto);
}
