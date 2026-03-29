package com.avira.authenticationservice.controller;

import com.avira.authenticationservice.dto.LoginRequest;
import com.avira.authenticationservice.dto.RefreshTokenRequest;
import com.avira.authenticationservice.dto.RegisterRequest;
import com.avira.authenticationservice.dto.TokenResponse;
import com.avira.authenticationservice.dto.UpdateUserRolesRequest;
import com.avira.authenticationservice.dto.UserResponse;
import com.avira.authenticationservice.dto.UserRolesResponse;
import com.avira.authenticationservice.fixture.RegisterRequestFixtures;
import com.avira.authenticationservice.service.AuthenticationOrchestrationService;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.constants.UserRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationOrchestrationService authenticationOrchestrationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void shouldRegister() {
        RegisterRequest request = new RegisterRequest("alice", "alice@avira.com", "StrongPass123", "Alice", "Smith");
        UserResponse expected = new UserResponse("kc-uuid-1", "alice", "alice@avira.com", "Alice", "Smith");
        when(authenticationOrchestrationService.register(request)).thenReturn(expected);

        ResponseEntity<UserResponse> response = authenticationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(authenticationOrchestrationService).register(request);
    }

    @Test
    void shouldPropagateConflictWhenUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest("alice", "alice@avira.com", "StrongPass123", "Alice", "Smith");
        when(authenticationOrchestrationService.register(request))
                .thenThrow(new ConflictException("User already exists: alice"));

        assertThatThrownBy(() -> authenticationController.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User already exists: alice");
        verify(authenticationOrchestrationService).register(request);
    }

    @Test
    void shouldLogin() {
        LoginRequest request = new LoginRequest("alice@avira.com", "StrongPass123");
        TokenResponse expected = new TokenResponse("access", 300, 1800, "refresh", "Bearer", "openid");
        when(authenticationOrchestrationService.login(request)).thenReturn(expected);

        ResponseEntity<TokenResponse> response = authenticationController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(authenticationOrchestrationService).login(request);
    }

    @Test
    void shouldRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        TokenResponse expected = new TokenResponse("access-2", 300, 1800, "refresh-2", "Bearer", "openid");
        when(authenticationOrchestrationService.refresh(request)).thenReturn(expected);

        ResponseEntity<TokenResponse> response = authenticationController.refreshToken(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(authenticationOrchestrationService).refresh(request);
    }

    @Test
    void shouldUpdateRoles() {
        String userId = "user-uuid-1";
        UpdateUserRolesRequest request = new UpdateUserRolesRequest(Set.of(UserRoles.USER, UserRoles.SELLER));
        UserRolesResponse expected = new UserRolesResponse(userId, Set.of(UserRoles.USER, UserRoles.SELLER));
        when(authenticationOrchestrationService.updateRoles(userId, request)).thenReturn(expected);

        ResponseEntity<UserRolesResponse> response = authenticationController.updateRoles(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(authenticationOrchestrationService).updateRoles(userId, request);
    }
}
