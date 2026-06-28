package com.eswar.authenticationservice.service;

import com.eswar.authenticationservice.dto.*;
import com.eswar.authenticationservice.exception.BusinessException;
import com.eswar.authenticationservice.exception.ErrorCode;
import com.eswar.authenticationservice.grpc.client.GrpcUserServiceClient;
import com.eswar.authenticationservice.grpc.mapper.GrpcExceptionMapper;
import com.eswar.grpc.user.UserResponse;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
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

    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticationResponseDto login(LoginRequestDto request) {

        UserResponse user;

        try {
            user = client.getUserByEmail(request.email());

        } catch (StatusRuntimeException ex) {
            log.warn("gRPC error during login", ex);
            throw GrpcExceptionMapper.map(ex);
        }


        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        UserDetails user2 =
                (UserDetails) authentication.getPrincipal();



        Set<String> roles = new HashSet<>(user.getRolesList());

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roles);

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

        if (request == null || request.refreshToken() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Invalid refresh token"
            );
        }
        jwtService.validateToken(request.refreshToken());

        String username = jwtService.extractUsername(request.refreshToken());

        UserResponse user;

        try {
            user = client.getUserByEmail(username);

        } catch (StatusRuntimeException ex) {
            log.warn("gRPC error during refresh", ex);
            throw com.eswar.authenticationservice.grpc.mapper.GrpcExceptionMapper.map(ex);
        }

        Set<String> roles = new HashSet<>(user.getRolesList());

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roles);

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