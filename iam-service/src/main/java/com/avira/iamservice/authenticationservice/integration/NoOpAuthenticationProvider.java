package com.avira.iamservice.authenticationservice.integration;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.avira.iamservice.authenticationservice.dto.TokenResponse;

@Component
@ConditionalOnProperty(prefix = "iam.auth", name = "provider", havingValue = "noop")
public class NoOpAuthenticationProvider implements AuthenticationProvider {

    @Override
    public TokenResponse login(String tenantId, String username, String password) {
        return new TokenResponse(
                "noop-access-" + UUID.randomUUID(),
                "noop-refresh-" + UUID.randomUUID(),
                "Bearer",
                300L,
                "NO_OP",
                tenantId,
                "iam-service",
                java.util.Map.of("tenant_id", tenantId, "sub", username)
        );
    }

    @Override
    public TokenResponse refresh(String tenantId, String refreshToken) {
        return new TokenResponse(
                "noop-access-" + UUID.randomUUID(),
                "noop-refresh-" + UUID.randomUUID(),
                "Bearer",
                300L,
                "NO_OP",
                tenantId,
                "iam-service",
                java.util.Map.of("tenant_id", tenantId)
        );
    }

    @Override
    public java.util.Map<String, Object> userInfo(String tenantId, String accessToken) {
        return java.util.Map.of("tenant_id", tenantId, "active", Boolean.TRUE);
    }

    @Override
    public boolean introspect(String tenantId, String accessToken) {
        return accessToken != null && !accessToken.isBlank();
    }

    @Override
    public String clientCredentialsToken(String tenantId) {
        return "noop-service-" + UUID.randomUUID();
    }

    @Override
    public void logout(String tenantId, String refreshToken) {
        // Intentionally no-op in base scaffold.
    }
}
