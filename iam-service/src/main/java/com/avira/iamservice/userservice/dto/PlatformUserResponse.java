package com.avira.iamservice.userservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlatformUserResponse(
        UUID id,
        String username,
        String email,
        String status,
        OffsetDateTime createdAt
) {
}

