package com.avira.authenticationservice.service;

import com.avira.authenticationservice.dto.LoginRequest;
import com.avira.authenticationservice.dto.RefreshTokenRequest;
import com.avira.authenticationservice.dto.RegisterRequest;
import com.avira.authenticationservice.dto.TokenResponse;
import com.avira.authenticationservice.dto.UpdateUserRolesRequest;
import com.avira.authenticationservice.dto.UserResponse;
import com.avira.authenticationservice.dto.UserRolesResponse;
import com.avira.authenticationservice.fixture.RegisterRequestFixtures;
import com.avira.commonlib.client.KeycloakTokenWebClient;
import com.avira.commonlib.constants.UserRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationOrchestrationServiceTest {

    @Mock
    private KeycloakUserRegistrationService keycloakUserRegistrationService;

    @Mock
    private KeycloakUserRoleService keycloakUserRoleService;

    @Mock
    private KeycloakTokenWebClient keycloakTokenWebClient;

    @InjectMocks
    private AuthenticationOrchestrationService authenticationOrchestrationService;

    @Test
    void shouldRegisterUserInKeycloak() {
        RegisterRequest request = new RegisterRequest("alice", "alice@avira.com", "StrongPass123", "Alice", "Smith");
        UserResponse expected = new UserResponse("kc-uuid-1", "alice", "alice@avira.com", "Alice", "Smith");
        when(keycloakUserRegistrationService.register(request)).thenReturn(expected);

        UserResponse result = authenticationOrchestrationService.register(request);

        assertThat(result).isEqualTo(expected);
        verify(keycloakUserRegistrationService).register(request);
    }

    @Test
    void shouldDelegateLoginToKeycloak() {
        LoginRequest request = new LoginRequest("alice@avira.com", "StrongPass123");
        TokenResponse expected = new TokenResponse("access", 300, 1800, "refresh", "Bearer", "openid");
        when(keycloakTokenWebClient.login("alice@avira.com", "StrongPass123")).thenReturn(Map.of(
                "access_token", "access",
                "expires_in", 300,
                "refresh_expires_in", 1800,
                "refresh_token", "refresh",
                "token_type", "Bearer",
                "scope", "openid"
        ));

        TokenResponse result = authenticationOrchestrationService.login(request);

        assertThat(result).isEqualTo(expected);
        verify(keycloakTokenWebClient).login("alice@avira.com", "StrongPass123");
    }

    @Test
    void shouldDelegateRefreshToKeycloak() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh");
        TokenResponse expected = new TokenResponse("access-2", 300, 1800, "refresh-2", "Bearer", "openid");
        when(keycloakTokenWebClient.refresh("refresh")).thenReturn(Map.of(
                "access_token", "access-2",
                "expires_in", 300,
                "refresh_expires_in", 1800,
                "refresh_token", "refresh-2",
                "token_type", "Bearer",
                "scope", "openid"
        ));

        TokenResponse result = authenticationOrchestrationService.refresh(request);

        assertThat(result).isEqualTo(expected);
        verify(keycloakTokenWebClient).refresh("refresh");
    }

    @Test
    void shouldDelegateUpdateRolesToRoleService() {
        String userId = "user-uuid-1";
        UpdateUserRolesRequest request = new UpdateUserRolesRequest(Set.of(UserRoles.USER, UserRoles.SELLER));
        UserRolesResponse expected = new UserRolesResponse(userId, Set.of(UserRoles.USER, UserRoles.SELLER));
        when(keycloakUserRoleService.updateRoles(userId, request.roles())).thenReturn(expected);

        UserRolesResponse result = authenticationOrchestrationService.updateRoles(userId, request);

        assertThat(result).isEqualTo(expected);
        verify(keycloakUserRoleService).updateRoles(userId, request.roles());
    }
}
