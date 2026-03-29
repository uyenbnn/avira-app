package com.avira.userservice.controller;

import com.avira.userservice.dto.CreateUserRequest;
import com.avira.userservice.dto.UserResponse;
import com.avira.userservice.enums.UserStatus;
import com.avira.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSyncControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnUserPage() {
        String userId = "domain-user-1";
        UserResponse user = UserResponse.builder()
                .id(userId)
                .email("alice@avira.com")
                .phone("0123456789")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<UserResponse> page = new PageImpl<>(List.of(user), pageRequest, 1);

        when(userService.findAll(pageRequest)).thenReturn(page);

        ResponseEntity<Page<UserResponse>> response = userController.findAll(pageRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst().email()).isEqualTo("alice@avira.com");
        verify(userService).findAll(pageRequest);
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest(
                "new@avira.com",
                "StrongPass123",
                "0987654321",
                "New",
                "User"
        );
        UserResponse created = UserResponse.builder()
                .id("domain-user-2")
                .email("new@avira.com")
                .phone("0987654321")
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userService.create(request)).thenReturn(created);

        ResponseEntity<UserResponse> response = userController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("new@avira.com");
        verify(userService).create(request);
    }
}
