package com.eswar.productservice.dto;

import com.eswar.productservice.constatnts.ProductSize;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductRequestDto(
        @NotBlank(message = "SKU is required")
        @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
        String sku,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Brand is required") // Don't forget brand!
        String brand,

        ProductSize productSize,

        @Size(max = 1000)
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotNull(message = "Featured flag is required")
        Boolean featured


) {
}
