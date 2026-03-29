package com.avira.authenticationservice.dto;

public record TokenResponse(
        String accessToken,
        int expiresIn,
        int refreshExpiresIn,
        String refreshToken,
        String tokenType,
        String scope
) {
}

