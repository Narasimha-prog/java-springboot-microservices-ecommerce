package com.lnreddy.cart_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemDTO(
        UUID productId,
        String name,
        String imageUrl,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
) {
}
