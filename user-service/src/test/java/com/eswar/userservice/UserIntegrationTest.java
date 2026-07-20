package com.eswar.userservice;

import com.eswar.userservice.constants.UserRole;
import com.eswar.userservice.dto.UserRequestDto;
import com.eswar.userservice.dto.UserResponseDto;
import com.eswar.userservice.repository.IUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // 🟢 Boots full application context
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
//  Enables HTTP endpoint simulation over full context
@Testcontainers // Activates Testcontainers Docker management lifecycle
public class UserIntegrationTest {


    @Container
    @ServiceConnection //  Automatically wires database credentials straight to Spring Data datasource config
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IUserRepository userRepository; // 🟢 Real repository instance to verify DB persistence state

    private UserRequestDto validUserRequest;

    @BeforeEach
    void setUp() {
        // Clear database baseline state to isolate tests clean
        userRepository.deleteAll();

        Set<UserRole> roles = new HashSet<>(Collections.singletonList(UserRole.USER));
        validUserRequest = new UserRequestDto(
                "John",                     // 1. firstName
                "Doe",                      // 2. lastName
                "integration@example.com",  // 3. email
                "SecurePassword123@",       // 4. password
                "+1",                       // 5. countryCode
                "1234567890",               // 6. phoneNumber
                "Main St",                  // 7. addressStreet
                "New York",                 // 8. addressCity
                "USA",                      // 9. addressCountry
                "10001",                    // 10. addressZipCode
                roles                       // 11. roles
        );
    }

    @Test
    @DisplayName("Full Pipeline Integration Flow: Create User -> Verify Database Persistence")
    void createAndVerifyUser_IntegrationFlow() throws Exception {

        // 1. ACT: Make real HTTP request to create a user profile
        String responseContent = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated()) // Expect HTTP 201
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDto createdUser = objectMapper.readValue(responseContent, UserResponseDto.class);

        // 2. ASSERT: Directly query database using your real repository to ensure records exist
        var databaseUser = userRepository.findById(createdUser.id());
        assertThat(databaseUser).isPresent();
        assertThat(databaseUser.get().getEmail()).isEqualTo("integration@example.com");
        assertThat(databaseUser.get().getFirstName()).isEqualTo("John");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Secure Route Integration: Prevent Duplicate Email Registration across Pipeline")
    void duplicateEmail_ReturnsConflictError() throws Exception {
        // Arrange: Populate an existing user directly into the real test database
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated());

        // Act & Assert: Attempt to submit identical user payload structure a second time
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isConflict()); //  Verifies real repository checks pass error handling Advice upwards!
    }
}
