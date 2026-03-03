package com.eswar.authenticationservice.rest;

import com.eswar.authenticationservice.dto.AccessTokenResponseDto;
import com.eswar.authenticationservice.dto.AuthenticationResponseDto;
import com.eswar.authenticationservice.dto.LoginRequestDto;
import com.eswar.authenticationservice.dto.RefreshTokenRequestDto;
import com.eswar.authenticationservice.service.IAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for managing authentication in the e-commerce platform")
public class AuthenticationRestController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "login user", description = "login entry for user")
    public ResponseEntity<AuthenticationResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "refresh token ", description = "to get access token using refresh token")
    public ResponseEntity<AccessTokenResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        return ResponseEntity.ok(authenticationService.refresh(request));
    }
}
