package com.avira.authenticationservice.dto;

public record UserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName
) {
}

