package com.avira.userservice.dto;

import com.avira.userservice.enums.UserStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record UserResponse(
        String id,
        String email,
        String phone,
        UserStatus status,
        boolean emailVerified,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt
) {}

