package com.avira.userservice.dto;

import com.avira.userservice.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record UserProfileResponse(
        UUID userId,
        String firstName,
        String lastName,
        String avatarUrl,
        LocalDate birthDate,
        String gender
) {}

