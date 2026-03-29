package com.avira.userservice.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserProfileResponse(
        String userId,
        String firstName,
        String lastName,
        String avatarUrl,
        LocalDate birthDate,
        String gender
) {}
