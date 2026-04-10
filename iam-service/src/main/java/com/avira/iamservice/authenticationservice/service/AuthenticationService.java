package com.avira.iamservice.authenticationservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.avira.iamservice.authenticationservice.dto.TokenResponse;
import com.avira.iamservice.authenticationservice.integration.AuthenticationProvider;

@Service
public class AuthenticationService {

    private final AuthenticationProvider authenticationProvider;

    public AuthenticationService(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    public TokenResponse login(String tenantId, String appId, String username, String password) {
        TokenResponse providerResponse = authenticationProvider.login(tenantId, username, password);
        Map<String, Object> userInfo = authenticationProvider.userInfo(tenantId, providerResponse.accessToken());

        if (!authenticationProvider.introspect(tenantId, providerResponse.accessToken())) {
            throw new IllegalArgumentException("Invalid access token for tenant: " + tenantId);
        }

        Map<String, Object> payload = toApplicationPayload(tenantId, appId, userInfo);
        return new TokenResponse(
                providerResponse.accessToken(),
                providerResponse.refreshToken(),
                providerResponse.tokenType(),
                providerResponse.expiresIn(),
                providerResponse.provider(),
                tenantId,
                appId,
                payload
        );
    }

    public TokenResponse refresh(String tenantId, String appId, String refreshToken) {
        TokenResponse providerResponse = authenticationProvider.refresh(tenantId, refreshToken);

        if (!authenticationProvider.introspect(tenantId, providerResponse.accessToken())) {
            throw new IllegalArgumentException("Invalid refreshed access token for tenant: " + tenantId);
        }

        Map<String, Object> userInfo = authenticationProvider.userInfo(tenantId, providerResponse.accessToken());
        Map<String, Object> payload = toApplicationPayload(tenantId, appId, userInfo);

        return new TokenResponse(
                providerResponse.accessToken(),
                providerResponse.refreshToken(),
                providerResponse.tokenType(),
                providerResponse.expiresIn(),
                providerResponse.provider(),
                tenantId,
                appId,
                payload
        );
    }

    public void logout(String tenantId, String refreshToken) {
        authenticationProvider.logout(tenantId, refreshToken);
    }

    private Map<String, Object> toApplicationPayload(String tenantId, String appId, Map<String, Object> userInfo) {
        Object realmAccess = userInfo.get("realm_access");
        List<String> roles = new ArrayList<>();
        if (realmAccess instanceof Map<?, ?> realmMap) {
            Object rolesValue = realmMap.get("roles");
            if (rolesValue instanceof List<?> roleList) {
                for (Object role : roleList) {
                    if (role != null) {
                        roles.add(role.toString());
                    }
                }
            }
        }

        return Map.of(
                "appId", appId,
                "tenantId", tenantId,
                "userId", String.valueOf(userInfo.getOrDefault("sub", "unknown")),
                "roles", roles
        );
    }
}
