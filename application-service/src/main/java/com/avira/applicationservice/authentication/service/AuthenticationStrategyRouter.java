package com.avira.applicationservice.authentication.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.avira.applicationservice.authentication.AuthMode;
import com.avira.applicationservice.authentication.strategy.AuthenticationHandlerStrategy;

@Service
public class AuthenticationStrategyRouter {
    private final Map<AuthMode, AuthenticationHandlerStrategy> strategyByMode;

    public AuthenticationStrategyRouter(List<AuthenticationHandlerStrategy> strategies) {
        this.strategyByMode = new EnumMap<>(AuthMode.class);
        for (AuthenticationHandlerStrategy strategy : strategies) {
            strategyByMode.put(strategy.supports(), strategy);
        }
    }

    public AuthenticationHandlerStrategy resolve(AuthMode mode) {
        AuthenticationHandlerStrategy strategy = strategyByMode.get(mode);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported auth mode: " + mode);
        }
        return strategy;
    }
}
