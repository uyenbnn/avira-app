package com.avira.iamservice.auth.dto;

public record LoginRequest(
        String tenantId,
        String username,
        String password,
        String appId
) {
}
