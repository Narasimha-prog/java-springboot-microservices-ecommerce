package com.eswar.orderservice.dto;

import com.eswar.orderservice.constants.OrderStatus;

import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
       UUID orderId,

     UUID customerId,

         OrderStatus status,

    List<OrderItemDto>items
) {
}
