package com.eswar.paymentservice.mapper;

import com.eswar.paymentservice.dto.PaymentCreateResponse;
import com.eswar.paymentservice.dto.PaymentResponse;
import com.eswar.paymentservice.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IPaymentMapper {

    // Entity → Response
    @Mapping(target = "message", constant = "Payment fetched")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    PaymentResponse toResponse(PaymentEntity entity);

    // Optional: Create response for frontend
    @Mapping(target = "razorpayOrderId", source = "razorpayOrderId")
    @Mapping(target = "amount", expression = "java(entity.getAmount().longValue() * 100)")
    @Mapping(target = "currency", constant = "INR")
    @Mapping(target = "key", ignore = true) // set manually
    PaymentCreateResponse toCreateResponse(PaymentEntity entity);
}
