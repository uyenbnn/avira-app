package com.avira.applicationinitializationservice.dto;

public record InitializationResponse(
        String realm,
        boolean realmCreated,
        boolean userClientCreated,
        boolean adminClientCreated,
        boolean anonymousUserCreated,
        boolean defaultAdminUserCreated
) {
}

