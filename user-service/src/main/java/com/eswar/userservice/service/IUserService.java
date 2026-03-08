package com.eswar.userservice.service;

import com.eswar.userservice.dto.UserGrpcResponse;
import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserResponseDto createUser(UserRequestDto request);
    UserResponseDto getUserById(UUID id);
    UserGrpcResponse getUserByGrpcUserId(UUID id);
    UserResponseDto getUserByEmail(String email);
    UserGrpcResponse getUserByGrpcUserEmail(String email);
    List<UserResponseDto> getAllUsers();
    void deleteUser(UUID id);
    UserResponseDto updateUser(UUID id, UserRequestDto request);
}
