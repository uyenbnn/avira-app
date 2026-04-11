package com.avira.applicationservice.authentication.strategy;

import org.springframework.stereotype.Component;

import com.avira.applicationservice.authentication.AuthMode;
import com.avira.applicationservice.authentication.dto.AppTokenResponse;
import com.avira.applicationservice.authentication.dto.TokenExchangeRequest;

@Component
public class CustomJwtAuthenticationHandlerStrategy implements AuthenticationHandlerStrategy {

    @Override
    public AuthMode supports() {
        return AuthMode.CUSTOM_JWT;
    }

    @Override
    public AppTokenResponse exchange(String tenantId, String appId, TokenExchangeRequest request) {
        return new AppTokenResponse(
                "custom-access-" + appId,
                "custom-refresh-" + appId,
                300,
                "Bearer",
                appId,
                tenantId,
                supports().name()
        );
    }
}
