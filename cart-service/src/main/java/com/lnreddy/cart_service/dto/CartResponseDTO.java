package com.lnreddy.cart_service.dto;

import java.math.BigDecimal;
import java.util.Set;

public record CartResponseDTO(
        String userId,
        Set<CartItemDTO> items,
        BigDecimal totalAmount
) {
}
