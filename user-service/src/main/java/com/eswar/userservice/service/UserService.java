package com.eswar.userservice.service;

import com.eswar.userservice.constants.ErrorMessages;
import com.eswar.userservice.dto.UserGrpcResponse;
import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.entity.UserEntity;
import com.eswar.userservice.exception.UserNotFoundException;
import com.eswar.userservice.mapper.IUserMapper;
import com.eswar.userservice.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final IUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // Create a new user
    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {
        UserEntity entity = userMapper.toEntity(request);

        String encodedPassword = passwordEncoder.encode(entity.getPassword());
        entity.setPassword(encodedPassword);

        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    // Get user by ID
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString() ));
        return userMapper.toResponse(entity);
    }

    // Get user by ID
    @Transactional(readOnly = true)
    public UserGrpcResponse getUserByGrpcUserId(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString() ));
        return userMapper.toGrpcResponse(entity);
    }

    // Get user by Email
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public UserGrpcResponse getUserByGrpcUserEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toGrpcResponse(entity);
    }

    // Get all users
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // Update user
    public UserResponseDto updateUser(UUID id, UserRequestDto request) {
        return userRepository.findById(id)
                .map(existing -> {
                    existing.setFirstName(request.firstName());
                    existing.setLastName(request.lastName());
                    existing.setEmail(request.email());
                    existing.setCountryCode(request.countryCode());
                    existing.setPhoneNumber(request.phoneNumber());
                    existing.setAddressStreet(request.addressStreet());
                    existing.setAddressCity(request.addressCity());
                    existing.setAddressCountry(request.addressCountry());
                    existing.setAddressZipCode(request.addressZipCode());
                    existing.setRoles(request.roles());

                    UserEntity updated = userRepository.save(existing);
                    return userMapper.toResponse(updated);
                })
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    // Delete user
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException( id.toString() );
        }
        userRepository.deleteById(id);
    }
}