package com.avira.userservice.user.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserProfileRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 512) String avatarUrl,
        LocalDate birthDate,
        @Size(max = 20) String gender
) {}

