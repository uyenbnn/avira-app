package com.avira.iamservice.auth.dto;

public record RefreshRequest(
        String tenantId,
        String refreshToken
) {
}
