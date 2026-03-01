package com.eswar.authenticationservice.mapper;

import com.eswar.authenticationservice.dto.AccessTokenResponseDto;
import com.eswar.authenticationservice.dto.AuthenticationRequestDto;
import com.eswar.authenticationservice.dto.AuthenticationResponseDto;
import com.eswar.authenticationservice.dto.LoginRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface IAuthenticationMapper {

    AuthenticationRequestDto toAuthRequest(LoginRequestDto request);

    @Mapping(target = "tokenType", constant = "Bearer")
    AuthenticationResponseDto toAuthenticationResponse(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            Instant issuedAt,
            String username,
            Set<String> roles
    );

    @Mapping(target = "tokenType", constant = "Bearer")
    AccessTokenResponseDto toAccessTokenResponse(
            String accessToken,
            Long expiresIn,
            Instant issuedAt,
            String username,
            String role
    );

}
