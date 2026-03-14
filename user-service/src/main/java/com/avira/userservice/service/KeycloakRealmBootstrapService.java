package com.avira.userservice.service;

import com.avira.userservice.constants.RoleConstants;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KeycloakRealmBootstrapService implements ApplicationRunner {

    private static final int MAX_ERROR_BODY_LEN = 500;

    /**
     * Client used by normal users to authenticate (login endpoint).
     * Public client — no secret needed from the browser/app side.
     */
    private static final String USER_CLIENT_SUFFIX = "-user-client";

    private final Keycloak keycloak;

    public KeycloakRealmBootstrapService(@Qualifier("keycloakMaster") Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @Value("${keycloak.sync.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.admin.realm:master}")
    private String adminRealm;

    @Value("${keycloak.sync.client-id:admin-cli}")
    private String clientId;

    @Value("${keycloak.sync.realm:avira}")
    private String targetRealm;

    @Value("${keycloak.realm.auto-create:false}")
    private boolean autoCreateRealm;

    @Value("${keycloak.realm.auto-create.fail-fast:true}")
    private boolean failFast;

    @Override
    public void run(ApplicationArguments args) {
        if (!autoCreateRealm) {
            return;
        }

        try {
            boolean exists = keycloak.realms().findAll().stream()
                    .anyMatch(realm -> targetRealm.equals(realm.getRealm()));

            if (exists) {
                log.info("Keycloak realm '{}' already exists — checking clients and roles", targetRealm);
                ensureBaseRolesInRealm();
                ensureAdminClientForRealm();
                ensureUserClientForRealm();
                return;
            }

            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(targetRealm);
            realm.setEnabled(true);

            keycloak.realms().create(realm);
            log.info("Created Keycloak realm '{}'", targetRealm);

            ensureBaseRolesInRealm();
            ensureAdminClientForRealm();
            ensureUserClientForRealm();
        } catch (Exception ex) {
            String details = buildBootstrapErrorDetails(ex);
            if (failFast) {
                throw new IllegalStateException("Keycloak realm bootstrap failed: " + details, ex);
            }
            log.error("Keycloak realm bootstrap failed: {}", details, ex);
        }
    }

    // ------------------------------------------------------------------ //
    //  Realm roles
    // ------------------------------------------------------------------ //

    /**
     * Ensures USER, ADMIN, SELLER, BUYER realm roles exist in the target realm.
     */
    private void ensureBaseRolesInRealm() {
        for (String roleName : RoleConstants.BASE_ROLES) {
            ensureRealmRole(roleName);
        }
    }

    private void ensureRealmRole(String roleName) {
        var rolesResource = keycloak.realm(targetRealm).roles();
        boolean exists = rolesResource.list().stream()
                .anyMatch(r -> roleName.equals(r.getName()));
        if (exists) {
            log.debug("Realm role '{}' already exists in realm '{}'", roleName, targetRealm);
            return;
        }
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription("Role: " + roleName);
        rolesResource.create(role);
        log.info("Created realm role '{}' in realm '{}'", roleName, targetRealm);
    }

    // ------------------------------------------------------------------ //
    //  Admin / service-account client (confidential)
    // ------------------------------------------------------------------ //

    /**
     * Creates a confidential client (service account) named after the realm
     * — used by admin back-end operations (e.g. user management via Admin API).
     */
    private void ensureAdminClientForRealm() {
        var clientsResource = keycloak.realm(targetRealm).clients();
        boolean exists = !clientsResource.findByClientId(targetRealm).isEmpty();
        if (exists) {
            log.info("Admin client '{}' already exists in realm '{}'", targetRealm, targetRealm);
            return;
        }

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(targetRealm);
        client.setName(targetRealm + " (admin/service-account)");
        client.setEnabled(true);
        client.setPublicClient(false);                  // confidential
        client.setDirectAccessGrantsEnabled(false);
        client.setServiceAccountsEnabled(true);         // client_credentials grant
        client.setStandardFlowEnabled(false);

        createClient(client, "admin client '" + targetRealm + "'");
    }

    // ------------------------------------------------------------------ //
    //  User-facing client (public, direct-access-grants)
    // ------------------------------------------------------------------ //

    /**
     * Creates a public client for normal user authentication
     * — used by the login endpoint (Resource Owner Password Credentials grant).
     *
     * clientId  = {realm}-user-client  (e.g. "avira-user-client")
     * type      = public (no client_secret required)
     * grants    = direct access grants enabled
     */
    private void ensureUserClientForRealm() {
        String userClientId = targetRealm + USER_CLIENT_SUFFIX;
        var clientsResource = keycloak.realm(targetRealm).clients();

        boolean exists = !clientsResource.findByClientId(userClientId).isEmpty();
        if (exists) {
            log.info("User client '{}' already exists in realm '{}'", userClientId, targetRealm);
            return;
        }

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(userClientId);
        client.setName(targetRealm + " (user authentication)");
        client.setEnabled(true);
        client.setPublicClient(true);                   // public — no secret needed
        client.setDirectAccessGrantsEnabled(true);      // Resource Owner Password Credentials
        client.setServiceAccountsEnabled(false);
        client.setStandardFlowEnabled(true);
        client.setImplicitFlowEnabled(false);

        createClient(client, "user client '" + userClientId + "'");
        log.info("Use '{}' as keycloak.auth.client-id for the login endpoint", userClientId);
    }

    // ------------------------------------------------------------------ //
    //  Shared helpers
    // ------------------------------------------------------------------ //

    private void createClient(ClientRepresentation client, String description) {
        try (Response response = keycloak.realm(targetRealm).clients().create(client)) {
            int status = response.getStatus();
            if (status >= 200 && status < 300) {
                log.info("Created Keycloak {} in realm '{}'", description, targetRealm);
                return;
            }
            String body = readResponseBody(response);
            throw new IllegalStateException("Failed to create " + description
                    + " in realm '" + targetRealm + "' (HTTP " + status + ")"
                    + (body == null || body.isBlank() ? "" : ", body=" + body));
        }
    }

    private String buildBootstrapErrorDetails(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("targetRealm=").append(targetRealm)
                .append(", adminRealm=").append(adminRealm)
                .append(", clientId=").append(clientId)
                .append(", serverUrl=").append(serverUrl);

        if (ex instanceof WebApplicationException webEx) {
            Response response = webEx.getResponse();
            if (response != null) {
                sb.append(", httpStatus=").append(response.getStatus());
                String body = readResponseBody(response);
                if (body != null && !body.isBlank()) {
                    sb.append(", responseBody=").append(body);
                }
            }
        }

        sb.append(", cause=").append(ex.getClass().getSimpleName())
                .append(": ").append(ex.getMessage());
        return sb.toString();
    }

    private String readResponseBody(Response response) {
        try {
            if (!response.hasEntity()) {
                return null;
            }
            String body = response.readEntity(String.class);
            if (body == null) {
                return null;
            }
            body = body.replace('\n', ' ').replace('\r', ' ').trim();
            return body.length() <= MAX_ERROR_BODY_LEN
                    ? body
                    : body.substring(0, MAX_ERROR_BODY_LEN) + "...(truncated)";
        } catch (Exception ignored) {
            return null;
        }
    }
}
