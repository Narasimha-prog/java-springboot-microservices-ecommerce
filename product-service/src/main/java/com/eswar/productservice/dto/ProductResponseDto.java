package com.eswar.productservice.dto;

import com.eswar.productservice.constatnts.ProductColor;
import com.eswar.productservice.constatnts.ProductSize;
import com.eswar.productservice.constatnts.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponseDto(
        UUID id,
        String sku,
        String name,
        String brand,          // New
        String description,
        BigDecimal price,
        ProductSize productSize,
        String productColor,
        Boolean featured,      // New
        ProductStatus status,
        UUID categoryId,
        String categoryName,
        List<String> imageUrls, // New: List of transformed URLs
        Instant createdAt,
        Instant updatedAt
) {
}
