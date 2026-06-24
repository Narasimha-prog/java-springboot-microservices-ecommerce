package com.eswar.userservice.rest;
import org.springframework.security.test.context.support.WithMockUser;
import com.eswar.userservice.constants.UserRole;
import com.eswar.userservice.dto.PageResponse;
import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates sending HTTP calls without opening network sockets

    @Autowired
    private ObjectMapper objectMapper; // Serializes Java Record objects to JSON strings

    @MockitoBean // Stubs out your service interface entirely
    private IUserService userService;
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UUID userId;
    private String userEmail;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;


    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEmail = "test@example.com";
        Instant now = Instant.now();
        Set<UserRole> roles = new HashSet<>(Collections.singletonList(UserRole.USER));

        userRequestDto = new UserRequestDto(
                "John",
                "Doe",
                "test@example.com",
                "Password@123",     // password
                "+1",               // countryCode
                "1234567890",       // phoneNumber
                "Main St",
                "New York",
                "USA",
                "10001",
                roles
        );
        userResponseDto = new UserResponseDto(
                userId, "John", "Doe", userEmail, "+1", "1234567890",
                "Main St", "New York", "USA", "10001", roles, now, now, now
        );
    }

    @Test
    @DisplayName("POST /api/v1/users - Success")
    void createUser() throws Exception {
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Success")
    void getUserById() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(userEmail));
    }

    @Test
    @DisplayName("GET /api/v1/users/email - Success")
    void getUserByEmail() throws Exception {
        when(userService.getUserByEmail(userEmail)).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/v1/users/email")
                        .param("email", userEmail) // Sets query parameter ?email=...
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail));
    }

    @Test
    @DisplayName("GET /api/v1/users - Paginated Success")
    void getAllUsers() throws Exception {
        PageResponse<UserResponseDto> pageResponse = new PageResponse<>(
                List.of(userResponseDto), 0, 10, 1L, 1, true
        );
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(userId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - Success")
    void updateUser() throws Exception {
        when(userService.updateUser(eq(userId), any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Success")
    void deleteUser() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent()); // Changes to isOk() if your controller returns 200
    }

    @Test
    @DisplayName("POST /api/v1/users/{id}/roles/{role} - Success")
    void addRole() throws Exception {
        when(userService.addRoleToUser(userId, UserRole.ADMIN)).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/v1/users/{id}/roles/{role}", userId, UserRole.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id}/roles/{role} - Success")
    @WithMockUser(roles = "ADMIN")
    void removeRole() throws Exception {
        when(userService.removeRoleFromUser(userId, UserRole.ADMIN)).thenReturn(userResponseDto);

        mockMvc.perform(delete("/api/v1/users/{id}/roles/remove", userId, UserRole.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }


}