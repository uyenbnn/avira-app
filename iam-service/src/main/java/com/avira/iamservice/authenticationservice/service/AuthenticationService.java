package com.avira.iamservice.authenticationservice.service;

import org.springframework.stereotype.Service;

import com.avira.iamservice.authenticationservice.dto.TokenResponse;
import com.avira.iamservice.authenticationservice.integration.AuthenticationProvider;

@Service
public class AuthenticationService {

    private final AuthenticationProvider authenticationProvider;

    public AuthenticationService(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    public TokenResponse login(String username, String password) {
        return authenticationProvider.login(username, password);
    }

    public TokenResponse refresh(String refreshToken) {
        return authenticationProvider.refresh(refreshToken);
    }

    public void logout(String refreshToken) {
        authenticationProvider.logout(refreshToken);
    }
}
