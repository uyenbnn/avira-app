package com.avira.applicationinitializationservice.dto;

import java.util.List;

public record InitializationResponse(
        String operation,
        KeycloakInitialization keycloak,
        MessagingInitialization messaging
) {

    public static InitializationResponse forAll(KeycloakInitialization keycloak,
                                                MessagingInitialization messaging) {
        return new InitializationResponse("all", keycloak, messaging);
    }

    public static InitializationResponse forKeycloak(KeycloakInitialization keycloak) {
        return new InitializationResponse("keycloak", keycloak, null);
    }

    public static InitializationResponse forMessaging(MessagingInitialization messaging) {
        return new InitializationResponse("messaging", null, messaging);
    }

    public record KeycloakInitialization(
            String realm,
            boolean realmCreated,
            boolean userClientCreated,
            boolean adminClientCreated,
            boolean anonymousUserCreated,
            boolean defaultAdminUserCreated
    ) {
    }

    public record MessagingInitialization(
            List<String> created,
            List<String> existing
    ) {
    }
}

