package com.avira.applicationservice.authentication.strategy;

import com.avira.applicationservice.authentication.AuthMode;
import com.avira.applicationservice.authentication.dto.AppTokenResponse;
import com.avira.applicationservice.authentication.dto.TokenExchangeRequest;

public interface AuthenticationHandlerStrategy {
    AuthMode supports();

    AppTokenResponse exchange(String tenantId, String appId, TokenExchangeRequest request);
}
