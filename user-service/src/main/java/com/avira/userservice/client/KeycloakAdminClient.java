package com.avira.userservice.client;

import com.avira.commonlib.config.properties.KeycloakProperties;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;

    // ------------------------------------------------------------------ //
    //  Read
    // ------------------------------------------------------------------ //

    public Optional<UserRepresentation> findByEmail(String email) {
        List<UserRepresentation> users = realm().users().searchByEmail(email, true);
        return users.stream().findFirst();
    }

    public Optional<UserRepresentation> findById(String keycloakId) {
        try {
            return Optional.ofNullable(realm().users().get(keycloakId).toRepresentation());
        } catch (Exception e) {
            log.warn("Keycloak user not found for id {}: {}", keycloakId, e.getMessage());
            return Optional.empty();
        }
    }

    public List<UserRepresentation> listUsers(int first, int max) {
        return realm().users().list(first, max);
    }

    // ------------------------------------------------------------------ //
    //  Write
    // ------------------------------------------------------------------ //

    /**
     * Creates a user in Keycloak and returns the newly assigned Keycloak UUID.
     */
    public Optional<String> createUser(String email, String firstName, String lastName,
                                       String plainPassword, boolean emailVerified) {
        UserRepresentation user = buildUserRepresentation(email, firstName, lastName, emailVerified);

        if (plainPassword != null) {
            user.setCredentials(List.of(buildPasswordCredential(plainPassword)));
        }

        try (Response response = realm().users().create(user)) {
            if (response.getStatus() == 201) {
                String location = response.getHeaderString("Location");
                String keycloakId = location.substring(location.lastIndexOf('/') + 1);
                log.info("Created Keycloak user {} -> id={}", email, keycloakId);
                return Optional.of(keycloakId);
            }
            log.error("Failed to create Keycloak user {}: HTTP {}", email, response.getStatus());
            return Optional.empty();
        }
    }

    public void updateUser(String keycloakId, String email, String firstName, String lastName,
                           boolean emailVerified) {
        UserRepresentation user = buildUserRepresentation(email, firstName, lastName, emailVerified);
        realm().users().get(keycloakId).update(user);
        log.info("Updated Keycloak user {}", keycloakId);
    }

    public void resetPassword(String keycloakId, String newPlainPassword) {
        realm().users().get(keycloakId).resetPassword(buildPasswordCredential(newPlainPassword));
        log.info("Reset password for Keycloak user {}", keycloakId);
    }

    public void deleteUser(String keycloakId) {
        realm().users().delete(keycloakId);
        log.info("Deleted Keycloak user {}", keycloakId);
    }

    public void assignRole(String keycloakId, String roleName) {
        var roleRepresentation = realm().roles().get(roleName).toRepresentation();
        realm().users().get(keycloakId).roles().realmLevel().add(List.of(roleRepresentation));
        log.info("Assigned role {} to Keycloak user {}", roleName, keycloakId);
    }

    public void removeRole(String keycloakId, String roleName) {
        var roleRepresentation = realm().roles().get(roleName).toRepresentation();
        realm().users().get(keycloakId).roles().realmLevel().remove(List.of(roleRepresentation));
        log.info("Removed role {} from Keycloak user {}", roleName, keycloakId);
    }

    public void setUserEnabled(String keycloakId, boolean enabled) {
        UserRepresentation user = realm().users().get(keycloakId).toRepresentation();
        user.setEnabled(enabled);
        realm().users().get(keycloakId).update(user);
        log.info("Updated Keycloak user status keycloakId={} enabled={}", keycloakId, enabled);
    }

    public void setUserEnabledByEmail(String email, boolean enabled) {
        Optional<UserRepresentation> userOpt = findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Cannot update Keycloak status. User not found by email={}", email);
            return;
        }

        UserRepresentation user = userOpt.get();
        user.setEnabled(enabled);
        realm().users().get(user.getId()).update(user);
        log.info("Updated Keycloak user status email={} enabled={}", email, enabled);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private RealmResource realm() {
        return keycloak.realm(keycloakProperties.getSync().getRealm());
    }

    private UserRepresentation buildUserRepresentation(String email, String firstName,
                                                        String lastName, boolean emailVerified) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(emailVerified);
        return user;
    }

    private CredentialRepresentation buildPasswordCredential(String plainPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(plainPassword);
        return credential;
    }
}
