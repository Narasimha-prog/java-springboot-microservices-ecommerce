package com.eswar.userservice.service;

import com.eswar.userservice.constants.ErrorMessages;
import com.eswar.userservice.dto.UserRequest;
import com.eswar.userservice.dto.UserResponse;
import com.eswar.userservice.entity.UserEntity;
import com.eswar.userservice.exception.UserNotFoundException;
import com.eswar.userservice.mapper.UserMapper;
import com.eswar.userservice.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final UserMapper userMapper;

    // Create a new user
    public UserResponse createUser(UserRequest request) {
        UserEntity entity = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    // Get user by ID
    public UserResponse getUserById(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString() ));
        return userMapper.toResponse(entity);
    }

    // Get user by Email
    public UserResponse getUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toResponse(entity);
    }

    // Get all users
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // Update user
    public UserResponse updateUser(UUID id, UserRequest request) {
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
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException( id.toString() );
        }
        userRepository.deleteById(id);
    }
}