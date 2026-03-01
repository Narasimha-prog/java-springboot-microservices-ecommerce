package com.eswar.authenticationservice.dto;

import java.time.Instant;

public record AccessTokenResponseDto(
        String accessToken,
        String tokenType,
        Long expiresIn,
        Instant issuedAt,
        String username,
        String role
) {
}
