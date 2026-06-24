package com.eswar.userservice.rest;

import com.eswar.userservice.constants.UserRole;
import com.eswar.userservice.dto.PageResponse;
import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for managing users in the e-commerce platform")
public class UserRestController {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;



    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with roles and returns the created user")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Operation(summary = "Get a user by ID", description = "Fetches a single user by UUID",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        UserResponseDto response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.details")
    @Operation(summary = "Get user by email", description = "Fetch a user by their email address",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Fetches all users in the system",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<PageResponse<UserResponseDto>> getAllUsers( @ParameterObject Pageable pageable) {

        PageResponse<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Operation(summary = "Update an existing user", description = "Updates a user by UUID and returns updated data",security = @SecurityRequirement( name="JWT"))
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Operation(summary = "Delete a user", description = "Deletes a user by UUID")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/roles/add")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a role to user")
    public ResponseEntity<UserResponseDto> addRole(@PathVariable UUID id, @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.addRoleToUser(id, role));
    }

    @PatchMapping("/{id}/roles/remove")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a role from user")
    public ResponseEntity<UserResponseDto> removeRole(@PathVariable UUID id, @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.removeRoleFromUser(id, role));
    }
}