package com.eswar.userservice.rest;

import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for managing users in the e-commerce platform")
public class UserRestController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with roles and returns the created user")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "Fetches a single user by UUID",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        UserResponseDto response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-email")
    @Operation(summary = "Get user by email", description = "Fetch a user by their email address",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping()
    @Operation(summary = "Get all users", description = "Fetches all users in the system",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user", description = "Updates a user by UUID and returns updated data",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by UUID")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}