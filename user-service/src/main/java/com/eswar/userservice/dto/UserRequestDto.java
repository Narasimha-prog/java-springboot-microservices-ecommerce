package com.eswar.userservice.dto;
import com.eswar.userservice.constants.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRequestDto(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @NotBlank(message = "Country code is required")
        @Size(max = 5, message = "Country code must be at most 5 characters")
        String countryCode,

        @NotBlank(message = "Phone number is required")
        @Size(max = 15, message = "Phone number must be at most 15 characters")
        String phoneNumber,

        @Size(max = 100, message = "Street address must be at most 100 characters")
        String addressStreet,

        @Size(max = 100, message = "City must be at most 100 characters")
        String addressCity,

        @Size(max = 100, message = "Country must be at most 100 characters")
        String addressCountry,

        @Size(max = 20, message = "Zip code must be at most 20 characters")
        String addressZipCode,

        Set<UserRole> roles

) {}
