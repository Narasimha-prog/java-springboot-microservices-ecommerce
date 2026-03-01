package com.eswar.authenticationservice.service;

import com.eswar.authenticationservice.dto.AccessTokenResponseDto;
import com.eswar.authenticationservice.dto.AuthenticationResponseDto;
import com.eswar.authenticationservice.dto.LoginRequestDto;
import com.eswar.authenticationservice.dto.RefreshTokenRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImp implements IAuthenticationService {

    private final JwtService jwtService;

    @Override
    public AuthenticationResponseDto login(LoginRequestDto request) {
        return null;
    }

    @Override
    public AccessTokenResponseDto refresh(RefreshTokenRequestDto request) {
        return null;
    }
}
