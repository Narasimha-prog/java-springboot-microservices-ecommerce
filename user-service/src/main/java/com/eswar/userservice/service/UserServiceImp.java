package com.eswar.userservice.service;

import com.eswar.userservice.constants.UserRole;
import com.eswar.userservice.dto.PageResponse;
import com.eswar.userservice.dto.UserGrpcResponse;
import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.entity.UserEntity;
import com.eswar.userservice.exception.BusinessException;
import com.eswar.userservice.exception.ErrorCode;
import com.eswar.userservice.exception.mapper.ExceptionMapper;
import com.eswar.userservice.mapper.IUserMapper;
import com.eswar.userservice.repository.IUserRepository;
import com.eswar.userservice.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.BufferOverflowException;
import java.util.List;
import java.util.UUID;

import static com.eswar.userservice.exception.ErrorCode.USER_NOT_FOUND;
import static org.apache.commons.lang.exception.ExceptionUtils.getRootCause;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements IUserService {

    private final IUserRepository userRepository;
    private final IUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final ExceptionMapper exceptionMapper;

    // Create a new user
    @Transactional
    @Override
    public UserResponseDto createUser(UserRequestDto request) {

        userValidator.validateCreateUser(request);

        UserEntity entity = userMapper.toEntity(request);


        String encodedPassword = passwordEncoder.encode(entity.getPassword());

        entity.setPassword(encodedPassword);

        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponse(saved);


    }

    // Get user by ID
    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserById(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, id));
        return userMapper.toResponse(entity);
    }

    // Get user by ID
    @Transactional(readOnly = true)
    @Override
    public UserGrpcResponse getUserByGrpcUserId(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, id));
        return userMapper.toGrpcResponse(entity);
    }

    // Get user by Email
    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, email));
        return userMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public UserGrpcResponse getUserByGrpcUserEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND,email));
        return userMapper.toGrpcResponse(entity);
    }

    // Get all users
    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserResponseDto> getAllUsers(Pageable pageable) {

        Page<UserEntity> page = userRepository.findAll(pageable);

        List<UserResponseDto> content = page.getContent()
                .stream()
                .map(userMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    // Update user
    @Transactional
    @Override
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
                    existing.setRoles( request.roles());

                    UserEntity updated = userRepository.save(existing);
                    return userMapper.toResponse(updated);
                })
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND,id));
    }

    // Delete user
    @Transactional
    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(USER_NOT_FOUND,id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDto removeRoleFromUser(UUID id, UserRole role) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND,id));

        // Because it's a Set, this is O(1) in Java
        user.getRoles().remove(role);

        // When the transaction commits, Hibernate checks the @UniqueConstraint
        // and generates: DELETE FROM user_roles WHERE user_id = ? AND role = ?
        UserEntity savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponseDto addRoleToUser(UUID id, UserRole role) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND,id));

        user.getRoles().add(role);
        UserEntity savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

}