package com.avira.iamservice.authenticationservice.service;

import com.avira.commonlib.client.KeycloakTokenWebClient;
import com.avira.iamservice.authenticationservice.dto.TokenResponse;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final KeycloakTokenWebClient keycloakTokenWebClient;

    public AuthenticationService(KeycloakTokenWebClient keycloakTokenWebClient) {
        this.keycloakTokenWebClient = keycloakTokenWebClient;
    }

    public TokenResponse login(String username, String password) {
        Map<String, Object> response = keycloakTokenWebClient.login(username, password);
        return toTokenResponse(response);
    }

    public TokenResponse refresh(String refreshToken) {
        Map<String, Object> response = keycloakTokenWebClient.refresh(refreshToken);
        return toTokenResponse(response);
    }

    public void logout(String refreshToken) {
        keycloakTokenWebClient.logout(refreshToken);
    }

    private TokenResponse toTokenResponse(Map<String, Object> response) {
        return new TokenResponse(
                asString(response.get("access_token")),
                asString(response.get("refresh_token")),
                asString(response.get("token_type")),
                asLong(response.get("expires_in")),
                asLong(response.get("refresh_expires_in")),
                asString(response.get("scope"))
        );
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}

