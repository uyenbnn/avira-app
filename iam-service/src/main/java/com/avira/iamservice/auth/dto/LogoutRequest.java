package com.avira.iamservice.auth.dto;

public record LogoutRequest(
        String tenantId,
        String refreshToken
) {
}
