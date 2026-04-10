package com.lnreddy.cart_service.dto;

import java.math.BigDecimal;

public record CartItemDTO(
        String productId,
        String name,
        String imageUrl,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
) {
}
