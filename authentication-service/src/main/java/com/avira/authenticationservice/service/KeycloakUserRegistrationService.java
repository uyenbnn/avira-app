package com.avira.authenticationservice.service;

import com.avira.authenticationservice.dto.RegisterRequest;
import com.avira.authenticationservice.dto.UserResponse;
import com.avira.commonlib.config.properties.ApplicationProperties;
import com.avira.commonlib.config.properties.KeycloakAuthProperties;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.UserDomainActions;
import com.avira.commonlib.constants.UserRoles;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.user.UserRegisteredEvent;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserRegistrationService {

    private final Keycloak keycloak;
    private final EventPublisher eventPublisher;
    private final KeycloakAuthProperties keycloakAuthProperties;
    private final ApplicationProperties applicationProperties;

    public UserResponse register(RegisterRequest request) {
        String userId = createUser(request);
        try {
            setPassword(userId, request.password());
            assignRole(userId, UserRoles.USER);
            publishRegisteredEvent(userId, request);
            log.info("Registered user '{}' in realm '{}'", request.username(), keycloakAuthProperties.getRealm());
            return new UserResponse(userId, request.username(), request.email(), request.firstName(), request.lastName());
        } catch (RuntimeException ex) {
            deleteUserQuietly(userId);
            throw ex;
        }
    }

    private String createUser(RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        try (Response response = keycloak.realm(keycloakAuthProperties.getRealm()).users().create(user)) {
            int status = response.getStatus();
            if (status == 409) {
                throw new ConflictException("User already exists: " + request.username());
            }
            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "Failed to create user '" + request.username() + "' in Keycloak (HTTP " + status + ")");
            }
            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    private void setPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);
        keycloak.realm(keycloakAuthProperties.getRealm()).users().get(userId).resetPassword(credential);
    }

    private void assignRole(String userId, String roleName) {
        RoleRepresentation role = keycloak.realm(keycloakAuthProperties.getRealm()).roles().get(roleName).toRepresentation();
        keycloak.realm(keycloakAuthProperties.getRealm()).users().get(userId).roles().realmLevel().add(List.of(role));
    }

    private void publishRegisteredEvent(String userId, RegisterRequest request) {
        UserRegisteredEvent payload = new UserRegisteredEvent(
                userId,
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName()
        );
        eventPublisher.publish(
                EventTopics.USER_DOMAIN,
                UserDomainActions.REGISTERED,
                applicationProperties.getName(),
                userId,
                userId,
                payload,
                java.util.Map.of()
        );
    }

    private void deleteUserQuietly(String userId) {
        try {
            keycloak.realm(keycloakAuthProperties.getRealm()).users().delete(userId);
            log.warn("Rolled back Keycloak user '{}' after downstream registration failure", userId);
        } catch (Exception cleanupError) {
            log.error("Failed to roll back Keycloak user '{}' after registration error: {}",
                    userId, cleanupError.getMessage(), cleanupError);
        }
    }
}

