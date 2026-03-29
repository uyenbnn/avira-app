package com.avira.userservice.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone
) {}
