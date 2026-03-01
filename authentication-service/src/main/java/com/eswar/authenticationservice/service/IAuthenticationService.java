package com.eswar.authenticationservice.service;

import com.eswar.authenticationservice.dto.AccessTokenResponseDto;
import com.eswar.authenticationservice.dto.AuthenticationResponseDto;
import com.eswar.authenticationservice.dto.LoginRequestDto;
import com.eswar.authenticationservice.dto.RefreshTokenRequestDto;

public interface IAuthenticationService {

    AuthenticationResponseDto login(LoginRequestDto request);

    AccessTokenResponseDto refresh(RefreshTokenRequestDto request);
}
