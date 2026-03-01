package com.eswar.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RefreshTokenRequestDto(
        @NotBlank(message = "refresh token is required")

        @Pattern(
                regexp = "^[A-Za-z0-9-_]+\\\\.[A-Za-z0-9-_]+\\\\.[A-Za-z0-9-_]+$",
                message = "Invalid refresh token format"
        )
        String refreshToken
) {
}
