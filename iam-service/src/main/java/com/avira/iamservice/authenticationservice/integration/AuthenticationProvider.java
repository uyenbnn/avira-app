package com.avira.iamservice.authenticationservice.integration;

import com.avira.iamservice.authenticationservice.dto.TokenResponse;

public interface AuthenticationProvider {

    TokenResponse login(String username, String password);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
