package com.avira.authenticationservice.service;

import com.avira.authenticationservice.dto.LoginRequest;
import com.avira.authenticationservice.dto.RefreshTokenRequest;
import com.avira.authenticationservice.dto.RegisterRequest;
import com.avira.authenticationservice.dto.TokenResponse;
import com.avira.authenticationservice.dto.UserResponse;
import com.avira.authenticationservice.dto.UpdateUserRolesRequest;
import com.avira.authenticationservice.dto.UserRolesResponse;
import com.avira.commonlib.client.KeycloakTokenWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationOrchestrationService {

    private final KeycloakUserRegistrationService keycloakUserRegistrationService;
    private final KeycloakUserRoleService keycloakUserRoleService;
    private final KeycloakTokenWebClient keycloakTokenWebClient;

    public UserResponse register(RegisterRequest request) {
        return keycloakUserRegistrationService.register(request);
    }

    public UserRolesResponse updateRoles(String userId, UpdateUserRolesRequest request) {
        return keycloakUserRoleService.updateRoles(userId, request.roles());
    }

    public TokenResponse login(LoginRequest request) {
        return toTokenResponse(keycloakTokenWebClient.login(request.email(), request.password()));
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        return toTokenResponse(keycloakTokenWebClient.refresh(request.refreshToken()));
    }

    private TokenResponse toTokenResponse(Map<String, Object> body) {
        if (body == null) {
            throw new IllegalStateException("Keycloak token response is empty");
        }
        return new TokenResponse(
                asString(body.get("access_token")),
                asInt(body.get("expires_in")),
                asInt(body.get("refresh_expires_in")),
                asString(body.get("refresh_token")),
                asString(body.get("token_type")),
                asString(body.get("scope"))
        );
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
