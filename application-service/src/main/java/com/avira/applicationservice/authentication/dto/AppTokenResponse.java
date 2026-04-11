package com.avira.applicationservice.authentication.dto;

public record AppTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        String appId,
        String tenantId,
        String strategy
) {
}
