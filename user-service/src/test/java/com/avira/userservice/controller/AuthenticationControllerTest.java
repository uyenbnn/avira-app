package com.avira.userservice.controller;

import com.avira.userservice.dto.LoginRequest;
import com.avira.userservice.dto.RegisterRequest;
import com.avira.userservice.dto.TokenResponse;
import com.avira.userservice.dto.UserResponse;
import com.avira.userservice.enums.UserStatus;
import com.avira.userservice.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest("alice@avira.com", "StrongPass123", "0123456789", "Alice", "Smith");
        UserResponse user = UserResponse.builder()
                .id("kc-user-id")
                .email("alice@avira.com")
                .phone("0123456789")
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(authenticationService.register(request)).thenReturn(user);

        ResponseEntity<UserResponse> response = authenticationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("alice@avira.com");
        verify(authenticationService).register(request);
    }

    @Test
    void shouldLogin() {
        LoginRequest request = new LoginRequest("alice@avira.com", "StrongPass123");
        TokenResponse tokenResponse = new TokenResponse(
                "access-token", 300, 1800, "refresh-token", "Bearer", "openid profile");

        when(authenticationService.login(request)).thenReturn(tokenResponse);

        ResponseEntity<TokenResponse> response = authenticationController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access-token");
        verify(authenticationService).login(request);
    }
}

