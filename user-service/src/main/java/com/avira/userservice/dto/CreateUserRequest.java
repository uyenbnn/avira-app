package com.avira.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,


        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone,

        String firstName,
        String lastName
) {}

