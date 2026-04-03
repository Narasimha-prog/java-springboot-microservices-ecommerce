package com.eswar.orderservice.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record OrderDto(
Set<OrderItemDto> items
) {
}
