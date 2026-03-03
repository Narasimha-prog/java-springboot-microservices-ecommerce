package com.eswar.authenticationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(

        @NotBlank(message = "email is required")
        @Size(min = 3, max = 50, message = "email must be between 3 and 50 characters")
        @Email(message = "email must be a valid format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password

) {}