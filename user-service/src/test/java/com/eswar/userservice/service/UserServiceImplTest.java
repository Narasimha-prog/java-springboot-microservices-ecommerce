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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private IUserRepository userRepository;
    @Mock private IUserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserValidator userValidator;
    @Mock private ExceptionMapper exceptionMapper;

    @InjectMocks private UserServiceImp userService;

    private UUID userId;
    private String userEmail;
    private UserRequestDto userRequestDto;
    private UserEntity userEntity;
    private UserResponseDto userResponseDto;
    private UserGrpcResponse userGrpcResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userEmail = "test@example.com";

        java.time.Instant now = java.time.Instant.now();

        Set<UserRole> roles = new HashSet<>(Collections.singletonList(UserRole.USER));

        // 1. Mock Request DTO (Matches what your service receives)
        userRequestDto = new UserRequestDto(
                "John", "Doe", userEmail, "+1", "1234567890",
                "Main St", "New York", "USA", "10001", "rawPassword123", roles
        );

        // 2. Mock Entity State (Matches database state tracking parameters)
        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
        userEntity.setEmail(userEmail);
        userEntity.setCountryCode("+1");
        userEntity.setPhoneNumber("1234567890");
        userEntity.setAddressStreet("Main St");
        userEntity.setAddressCity("New York");
        userEntity.setAddressCountry("USA");
        userEntity.setAddressZipCode("10001");
        userEntity.setPassword("rawPassword123");
        userEntity.setRoles(roles);
        // Assuming your entity fields align with tracking metrics:
        userEntity.setCreatedAt(now);
        userEntity.setUpdatedAt(now);
        userEntity.setLastSeen(now);

        // 3. FIXED: Fully Populated Response DTO matching your exact Record fields
        userResponseDto = new UserResponseDto(
                userId,
                "John",
                "Doe",
                userEmail,
                "+1",
                "1234567890",
                "Main St",
                "New York",
                "USA",
                "10001",
                roles,
                now,   // createdAt
                now,   // updatedAt
                now    // lastSeen
        );

        userGrpcResponse = new UserGrpcResponse(userId,userEmail,"John","13re5chtfuy","Doe",roles);
    }
    // --- CREATE USER TESTS ---

    @Test
    @DisplayName("createUser - Success path")
    void createUser_Success() {
        doNothing().when(userValidator).validateCreateUser(userRequestDto);
        when(userRepository.existsByEmail(userEmail)).thenReturn(false);
        when(userMapper.toEntity(userRequestDto)).thenReturn(userEntity);
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(userEntity.getPassword()).isEqualTo("encodedPassword123");
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    @DisplayName("createUser - Should throw exception when email already exists")
    void createUser_ThrowsException_WhenEmailExists() {
        doNothing().when(userValidator).validateCreateUser(userRequestDto);
        when(userRepository.existsByEmail(userEmail)).thenReturn(true);

        BusinessException exception =assertThrows(
                BusinessException.class,
                () -> userService.createUser(userRequestDto)
        );
        assertThat(exception.getErrorCode()).isEqualTo("USER_ALREADY_EXISTS");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // --- GET USER BY ID TESTS ---

    @Test
    @DisplayName("getUserById - Success path")
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("getUserById - Should throw exception when user not found")
    void getUserById_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(BusinessException.class);
    }

    // --- GRPC LOOKUP TESTS ---

    @Test
    @DisplayName("getUserByGrpcUserId - Success path")
    void getUserByGrpcUserId_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toGrpcResponse(userEntity)).thenReturn(userGrpcResponse);

        UserGrpcResponse result = userService.getUserByGrpcUserId(userId);

        assertThat(result).isNotNull();
    }

    // --- GET USER BY EMAIL TESTS ---

    @Test
    @DisplayName("getUserByEmail - Success path")
    void getUserByEmail_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserByEmail(userEmail);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(userEmail);
    }

    // --- PAGINATION / GET ALL TESTS ---

    @Test
    @DisplayName("getAllUsers - Success path")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userPage = new PageImpl<>(List.of(userEntity), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponseDto);

        PageResponse<UserResponseDto> result = userService.getAllUsers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    // --- UPDATE USER TESTS ---

    @Test
    @DisplayName("updateUser - Success path")
    void updateUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser(userId, userRequestDto);

        assertThat(result).isNotNull();
        verify(userRepository).save(userEntity);
    }

    // --- DELETE USER TESTS ---

    @Test
    @DisplayName("deleteUser - Success path")
    void deleteUser_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("deleteUser - Should throw exception when user doesn't exist")
    void deleteUser_NotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    // --- ROLE MANAGEMENT MUTATION TESTS ---

    @Test
    @DisplayName("removeRoleFromUser - Success path")
    void removeRoleFromUser_Success() {
        userEntity.getRoles().add(UserRole.ADMIN); // Starts with [USER, ADMIN]
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.removeRoleFromUser(userId, UserRole.ADMIN);

        assertThat(result).isNotNull();
        assertThat(userEntity.getRoles()).doesNotContain(UserRole.ADMIN);
    }

    @Test
    @DisplayName("addRoleToUser - Success path")
    void addRoleToUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.addRoleToUser(userId, UserRole.ADMIN);

        assertThat(result).isNotNull();
        assertThat(userEntity.getRoles()).contains(UserRole.ADMIN);
    }
}