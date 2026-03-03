package com.eswar.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RefreshTokenRequestDto(
        @NotBlank(message = "refresh token is required")
        String refreshToken
) {
}
