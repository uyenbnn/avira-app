package com.avira.applicationservice.authentication.strategy;

import org.springframework.stereotype.Component;

import com.avira.applicationservice.authentication.AuthMode;
import com.avira.applicationservice.authentication.dto.AppTokenResponse;
import com.avira.applicationservice.authentication.dto.TokenExchangeRequest;

@Component
public class KeycloakAuthenticationHandlerStrategy implements AuthenticationHandlerStrategy {

    @Override
    public AuthMode supports() {
        return AuthMode.KEYCLOAK;
    }

    @Override
    public AppTokenResponse exchange(String tenantId, String appId, TokenExchangeRequest request) {
        return new AppTokenResponse(
                "keycloak-access-" + appId,
                "keycloak-refresh-" + appId,
                300,
                "Bearer",
                appId,
                tenantId,
                supports().name()
        );
    }
}
