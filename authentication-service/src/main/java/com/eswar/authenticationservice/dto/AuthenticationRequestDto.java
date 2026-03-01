package com.eswar.authenticationservice.dto;

public record AuthenticationRequestDto(
        String email,
        String password
) {
}
