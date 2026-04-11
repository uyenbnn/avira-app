package com.avira.applicationservice.authentication.service;

import org.springframework.stereotype.Service;

import com.avira.applicationservice.authentication.dto.AppTokenResponse;
import com.avira.applicationservice.authentication.dto.TokenExchangeRequest;

@Service
public class TokenExchangeService {
    private final AuthenticationStrategyRouter strategyRouter;

    public TokenExchangeService(AuthenticationStrategyRouter strategyRouter) {
        this.strategyRouter = strategyRouter;
    }

    public AppTokenResponse exchange(String tenantId, String appId, TokenExchangeRequest request) {
        return strategyRouter.resolve(request.authMode()).exchange(tenantId, appId, request);
    }
}
