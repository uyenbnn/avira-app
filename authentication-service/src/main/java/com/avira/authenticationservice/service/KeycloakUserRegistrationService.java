package com.avira.authenticationservice.service;

import com.avira.authenticationservice.dto.RegisterRequest;
import com.avira.authenticationservice.dto.UserResponse;
import com.avira.commonlib.constants.UserRoles;
import com.avira.commonlib.exception.ConflictException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserRegistrationService {

    private final Keycloak keycloak;

    @Value("${keycloak.auth.realm:avira}")
    private String realm;

    public UserResponse register(RegisterRequest request) {
        String userId = createUser(request);
        setPassword(userId, request.password());
        assignRole(userId, UserRoles.USER);
        log.info("Registered user '{}' in realm '{}'", request.username(), realm);
        return new UserResponse(userId, request.username(), request.email(), request.firstName(), request.lastName());
    }

    private String createUser(RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        try (Response response = keycloak.realm(realm).users().create(user)) {
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
        keycloak.realm(realm).users().get(userId).resetPassword(credential);
    }

    private void assignRole(String userId, String roleName) {
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(role));
    }
}

