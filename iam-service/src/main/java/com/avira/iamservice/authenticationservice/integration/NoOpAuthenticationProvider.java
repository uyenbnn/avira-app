package com.avira.iamservice.authenticationservice.integration;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.avira.iamservice.authenticationservice.dto.TokenResponse;

@Component
public class NoOpAuthenticationProvider implements AuthenticationProvider {

    @Override
    public TokenResponse login(String username, String password) {
        return new TokenResponse(
                "noop-access-" + UUID.randomUUID(),
                "noop-refresh-" + UUID.randomUUID(),
                "Bearer",
                300L,
                "NO_OP"
        );
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        return new TokenResponse(
                "noop-access-" + UUID.randomUUID(),
                "noop-refresh-" + UUID.randomUUID(),
                "Bearer",
                300L,
                "NO_OP"
        );
    }

    @Override
    public void logout(String refreshToken) {
        // Intentionally no-op in base scaffold.
    }
}
