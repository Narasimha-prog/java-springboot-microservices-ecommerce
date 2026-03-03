package com.eswar.authenticationservice.dto;

import java.time.Instant;
import java.util.Set;

public record AccessTokenResponseDto(
        String accessToken,
        String tokenType,
        Long expiresIn,
        Instant issuedAt,
        String username,
        Set<String> roles
) {
}
