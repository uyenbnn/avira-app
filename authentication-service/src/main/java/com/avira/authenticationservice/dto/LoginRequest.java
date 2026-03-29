package com.avira.authenticationservice.dto;

public record LoginRequest(
        String email,
        String password
) {
}

