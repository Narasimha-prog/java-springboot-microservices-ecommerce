package com.eswar.orderservice.dto;

import java.util.List;
import java.util.UUID;

public record OrderDto(
UUID customerId,
List<OrderItemDto> items
) {
}
