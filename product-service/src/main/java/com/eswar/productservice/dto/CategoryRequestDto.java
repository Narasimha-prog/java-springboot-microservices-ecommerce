package com.eswar.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CategoryRequestDto(
        @NotBlank(message = "Category name is required")
        String name,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description
) implements Serializable {
}
