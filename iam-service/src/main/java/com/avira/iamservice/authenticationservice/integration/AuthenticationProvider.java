package com.avira.iamservice.authenticationservice.integration;

import com.avira.iamservice.authenticationservice.dto.TokenResponse;

public interface AuthenticationProvider {

    TokenResponse login(String tenantId, String username, String password);

    TokenResponse refresh(String tenantId, String refreshToken);

    java.util.Map<String, Object> userInfo(String tenantId, String accessToken);

    boolean introspect(String tenantId, String accessToken);

    String clientCredentialsToken(String tenantId);

    void logout(String tenantId, String refreshToken);
}
