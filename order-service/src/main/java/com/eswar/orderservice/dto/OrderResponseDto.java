package com.eswar.orderservice.dto;

import com.eswar.orderservice.constants.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
       UUID orderId,

         UUID customerId,

         OrderStatus status,
       BigDecimal totalPrice,
       List<OrderItemDto> items
) {
}
