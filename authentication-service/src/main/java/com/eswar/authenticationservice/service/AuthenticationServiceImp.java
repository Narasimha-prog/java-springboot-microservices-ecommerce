package com.eswar.authenticationservice.service;

import com.eswar.authenticationservice.dto.AccessTokenResponseDto;
import com.eswar.authenticationservice.dto.AuthenticationResponseDto;
import com.eswar.authenticationservice.dto.LoginRequestDto;
import com.eswar.authenticationservice.dto.RefreshTokenRequestDto;
import com.eswar.authenticationservice.exception.UserNotFoundException;
import com.eswar.authenticationservice.exception.UserServiceUnavailableException;
import com.eswar.authenticationservice.grpc.client.GrpcUserServiceClient;
import com.eswar.grpc.user.UserResponse;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImp implements IAuthenticationService {

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    private final JwtService jwtService;
    private final GrpcUserServiceClient client;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationResponseDto login(LoginRequestDto request) {

       UserResponse user;
        try {
          user = client.getUserByEmail(request.email());
        } catch (StatusRuntimeException ex) {

log.warn("grpc error from getUserByEmail",ex);
            switch (ex.getStatus().getCode()) {

                case NOT_FOUND ->
                        throw new UserNotFoundException(request.email());

                case UNAVAILABLE ->
                        throw new UserServiceUnavailableException("User service unavailable");

                default ->
                        throw new RuntimeException("Unexpected gRPC error");
            }
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        log.info("Received user from gRPC: {}", user.getEmail());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Set<String> roles = new HashSet<>(user.getRolesList());

        String accessToken = jwtService.generateAccessToken(user.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthenticationResponseDto(
                accessToken,
                refreshToken,
                "JWT",
                accessExpiration / 1000,
                Instant.now(),
                user.getEmail(),
                roles
        );
    }

    @Override
    public AccessTokenResponseDto refresh(RefreshTokenRequestDto request) {

        if (request == null || request.refreshToken() == null
                || !jwtService.isTokenValid(request.refreshToken())) {

            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(request.refreshToken());


        UserResponse user;
        try {
            user = client.getUserByEmail(username);
        } catch (StatusRuntimeException ex) {

            switch (ex.getStatus().getCode()) {

                case NOT_FOUND ->
                        throw new UserNotFoundException("User not found");

                case UNAVAILABLE ->
                        throw new UserServiceUnavailableException("User service unavailable");

                default ->
                        throw new RuntimeException("Unexpected gRPC error");
            }
        }


        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        Set<String> roles = new HashSet<>(user.getRolesList());

        String accessToken = jwtService.generateAccessToken(user.getEmail(), roles);

        return new AccessTokenResponseDto(
                accessToken,
                "JWT",
                accessExpiration / 1000,
                Instant.now(),
                user.getEmail(),
                roles
        );
    }
}