package com.avira.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone
) {}

