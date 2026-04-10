package com.avira.iamservice.authenticationservice.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String provider,
        String tenantId,
        String appId,
        java.util.Map<String, Object> applicationTokenPayload
) {
}
