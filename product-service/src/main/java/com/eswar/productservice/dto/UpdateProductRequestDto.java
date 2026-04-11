package com.eswar.productservice.dto;

import com.eswar.productservice.constatnts.ProductSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequestDto(
        @NotBlank(message = "Name is required")
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        @Positive(message = "Price must be positive")
        BigDecimal price,

        // Add the flexible fields we discussed
        @NotBlank(message = "Color is required")
        String productColor,

        @NotNull(message = "Size is required")
        ProductSize productSize,

        @NotBlank(message = "Brand is required")
        String brand,

        @NotNull(message = "Category is required")
        UUID categoryId
) {
}
