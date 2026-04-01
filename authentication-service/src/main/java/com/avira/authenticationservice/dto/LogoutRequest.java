package com.avira.authenticationservice.dto;

public record LogoutRequest(
        String refreshToken
) {
}

