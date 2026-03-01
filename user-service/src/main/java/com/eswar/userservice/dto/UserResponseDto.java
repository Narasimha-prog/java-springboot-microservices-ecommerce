package com.eswar.userservice.dto;

import com.eswar.userservice.constants.UserRole;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
         String firstName,
         String lastName,
         String email,
         String countryCode,
         String phoneNumber,
         String addressStreet,
         String addressCity,
         String addressCountry,
         String addressZipCode,
         Set<UserRole> roles,
         Instant createdAt,
         Instant updatedAt,
         Instant lastSeen
) {
}
