package com.avira.applicationinitializationservice.service;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import com.avira.commonlib.constants.UserRoles;
import com.avira.commonlib.messaging.tenant.TenantAuthenticationEnabledEvent;
import com.avira.commonlib.messaging.tenant.TenantCreatedEvent;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakInitializationService {

    private static final String USER_CLIENT_SUFFIX = "-user-client";
    private static final int MAX_ERROR_BODY_LEN = 500;

    private final @Qualifier("keycloakMaster") Keycloak keycloak;

    @Value("${keycloak.sync.realm:avira}")
    private String targetRealm;

    @Value("${keycloak.seed.anonymous.username:anonymous}")
    private String anonymousUsername;

    @Value("${keycloak.seed.anonymous.email:anonymous@avira.local}")
    private String anonymousEmail;

    @Value("${keycloak.seed.anonymous.password:anonymous123}")
    private String anonymousPassword;

    @Value("${keycloak.seed.admin.username:avira-admin}")
    private String defaultAdminUsername;

    @Value("${keycloak.seed.admin.email:admin@avira.local}")
    private String defaultAdminEmail;

    @Value("${keycloak.seed.admin.password:admin123}")
    private String defaultAdminPassword;

    @Value("${keycloak.seed.admin-client.secret:}")
    private String adminClientSecret;

    @Value("${keycloak.seed.admin-client.realm-management-roles:view-users,query-users,manage-users,view-realm}")
    private List<String> adminClientRealmManagementRoles;

    @Value("${keycloak.sync.tenant-realm-prefix:tenant-}")
    private String tenantRealmPrefix;

    public InitializationResponse.KeycloakInitialization initializeKeycloak() {
        boolean realmCreated = ensureRealm();
        ensureBaseRoles();
        boolean userClientCreated = ensureUserAuthClient();
        boolean adminClientCreated = ensureAdminClient();
        boolean anonymousUserCreated = ensureUser(anonymousUsername, anonymousEmail, "Anonymous", "User", anonymousPassword, UserRoles.ANONYMOUS);
        boolean defaultAdminUserCreated = ensureUser(defaultAdminUsername, defaultAdminEmail, "Default", "Admin", defaultAdminPassword, UserRoles.ADMIN);

        return new InitializationResponse.KeycloakInitialization(
                targetRealm,
                realmCreated,
                userClientCreated,
                adminClientCreated,
                anonymousUserCreated,
                defaultAdminUserCreated
        );
    }

    public synchronized void initializeTenantKeycloak(TenantCreatedEvent event) {
        String previousRealm = targetRealm;
        try {
            targetRealm = resolveTenantRealm(event.tenantId());
            boolean realmCreated = ensureRealm();
            ensureBaseRoles();
            boolean userClientCreated = ensureUserAuthClient();
            log.info("Initialized tenant Keycloak realm='{}' tenantId={} realmCreated={} userClientCreated={}",
                    targetRealm, event.tenantId(), realmCreated, userClientCreated);
        } finally {
            targetRealm = previousRealm;
        }
    }

    public synchronized void initializeTenantKeycloak(TenantAuthenticationEnabledEvent event) {
        String previousRealm = targetRealm;
        try {
            targetRealm = resolveTenantRealm(event.tenantId());
            boolean realmCreated = ensureRealm();
            ensureBaseRoles();
            boolean userClientCreated = ensureUserAuthClient();
            log.info("Initialized tenant Keycloak realm='{}' tenantId={} realmCreated={} userClientCreated={}",
                    targetRealm, event.tenantId(), realmCreated, userClientCreated);
        } finally {
            targetRealm = previousRealm;
        }
    }

    private String resolveTenantRealm(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId is required to initialize tenant Keycloak realm");
        }
        return tenantRealmPrefix + tenantId;
    }

    private boolean ensureRealm() {
        boolean exists = keycloak.realms().findAll().stream()
                .anyMatch(realm -> targetRealm.equals(realm.getRealm()));
        if (exists) {
            return false;
        }

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(targetRealm);
        realm.setEnabled(true);
        keycloak.realms().create(realm);
        log.info("Created realm '{}'", targetRealm);
        return true;
    }

    private void ensureBaseRoles() {
        ensureRealmRole(UserRoles.USER);
        ensureRealmRole(UserRoles.ADMIN);
        ensureRealmRole(UserRoles.SELLER);
        ensureRealmRole(UserRoles.BUYER);
        ensureRealmRole(UserRoles.ANONYMOUS);
    }

    private void ensureRealmRole(String roleName) {
        var roles = keycloak.realm(targetRealm).roles();
        boolean exists = roles.list().stream().anyMatch(role -> roleName.equals(role.getName()));
        if (exists) {
            return;
        }

        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription("Seed role " + roleName);
        roles.create(role);
    }

    private boolean ensureUserAuthClient() {
        String clientId = targetRealm + USER_CLIENT_SUFFIX;
        var clients = keycloak.realm(targetRealm).clients();
        if (!clients.findByClientId(clientId).isEmpty()) {
            return false;
        }

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setName(targetRealm + " (user authentication)");
        client.setEnabled(true);
        client.setPublicClient(true);
        client.setDirectAccessGrantsEnabled(true);
        client.setServiceAccountsEnabled(false);
        client.setStandardFlowEnabled(true);
        client.setImplicitFlowEnabled(false);

        createClient(client, "user auth client '" + clientId + "'");
        return true;
    }

    private boolean ensureAdminClient() {
        var clients = keycloak.realm(targetRealm).clients();
        var found = clients.findByClientId(targetRealm);
        if (!found.isEmpty()) {
            ensureAdminClientSettings(found.getFirst());
            ensureAdminServiceAccountPermissions(found.getFirst());
            return false;
        }

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(targetRealm);
        client.setName(targetRealm + " (admin/service-account)");
        client.setProtocol("openid-connect");
        client.setEnabled(true);
        client.setPublicClient(false);
        if (adminClientSecret != null && !adminClientSecret.isBlank()) {
            client.setSecret(adminClientSecret);
        }
        client.setDirectAccessGrantsEnabled(false);
        client.setServiceAccountsEnabled(true);
        client.setStandardFlowEnabled(false);
        client.setImplicitFlowEnabled(false);

        createClient(client, "admin client '" + targetRealm + "'");

        var created = clients.findByClientId(targetRealm);
        if (!created.isEmpty()) {
            ensureAdminClientSettings(created.getFirst());
            ensureAdminServiceAccountPermissions(created.getFirst());
        }
        return true;
    }

    private void ensureAdminClientSettings(ClientRepresentation existingClient) {
        if (existingClient == null || existingClient.getId() == null || existingClient.getId().isBlank()) {
            return;
        }

        boolean needsUpdate = !Boolean.TRUE.equals(existingClient.isEnabled())
                || !Boolean.FALSE.equals(existingClient.isPublicClient())
                || !Boolean.TRUE.equals(existingClient.isServiceAccountsEnabled())
                || !Boolean.FALSE.equals(existingClient.isDirectAccessGrantsEnabled())
                || !Boolean.FALSE.equals(existingClient.isStandardFlowEnabled())
                || !Boolean.FALSE.equals(existingClient.isImplicitFlowEnabled())
                || (existingClient.getProtocol() != null && !"openid-connect".equals(existingClient.getProtocol()));

        if (!needsUpdate) {
            return;
        }

        ClientRepresentation update = new ClientRepresentation();
        update.setId(existingClient.getId());
        update.setClientId(existingClient.getClientId());
        update.setName(existingClient.getName());
        update.setEnabled(true);
        update.setProtocol("openid-connect");
        update.setPublicClient(false);
        update.setServiceAccountsEnabled(true);
        update.setDirectAccessGrantsEnabled(false);
        update.setStandardFlowEnabled(false);
        update.setImplicitFlowEnabled(false);
        if (adminClientSecret != null && !adminClientSecret.isBlank()) {
            update.setSecret(adminClientSecret);
        }

        keycloak.realm(targetRealm).clients().get(existingClient.getId()).update(update);
    }

    private void ensureAdminServiceAccountPermissions(ClientRepresentation adminClient) {
        if (adminClient == null || adminClient.getId() == null || adminClient.getId().isBlank()) {
            return;
        }

        var clients = keycloak.realm(targetRealm).clients();
        var realmManagement = clients.findByClientId("realm-management");
        if (realmManagement.isEmpty() || realmManagement.getFirst().getId() == null || realmManagement.getFirst().getId().isBlank()) {
            log.warn("Skipped admin service-account role mapping because realm-management client was not found in realm '{}'", targetRealm);
            return;
        }

        String realmManagementClientUuid = realmManagement.getFirst().getId();
        UserRepresentation serviceAccount = clients.get(adminClient.getId()).getServiceAccountUser();
        if (serviceAccount == null || serviceAccount.getId() == null || serviceAccount.getId().isBlank()) {
            log.warn("Skipped admin service-account role mapping because service account is unavailable for client '{}' in realm '{}'",
                    targetRealm, targetRealm);
            return;
        }

        var serviceAccountRoles = keycloak.realm(targetRealm).users().get(serviceAccount.getId()).roles().clientLevel(realmManagementClientUuid);
        Set<String> existingRoleNames = serviceAccountRoles.listAll().stream()
                .map(RoleRepresentation::getName)
                .collect(java.util.stream.Collectors.toSet());

        List<RoleRepresentation> missingRoles = adminClientRealmManagementRoles.stream()
                .filter(roleName -> !existingRoleNames.contains(roleName))
                .map(roleName -> keycloak.realm(targetRealm)
                        .clients()
                        .get(realmManagementClientUuid)
                        .roles()
                        .get(roleName)
                        .toRepresentation())
                .toList();

        if (!missingRoles.isEmpty()) {
            serviceAccountRoles.add(missingRoles);
            log.info("Granted realm-management roles {} to admin client service account in realm '{}'",
                    missingRoles.stream().map(RoleRepresentation::getName).toList(),
                    targetRealm);
        }
    }

    private void createClient(ClientRepresentation client, String description) {
        try (Response response = keycloak.realm(targetRealm).clients().create(client)) {
            int status = response.getStatus();
            if (status >= 200 && status < 300) {
                return;
            }
            String body = readResponseBody(response);
            throw new IllegalStateException("Failed to create " + description
                    + " in realm '" + targetRealm + "' (HTTP " + status + ")"
                    + (body == null || body.isBlank() ? "" : ", body=" + body));
        }
    }

    private boolean ensureUser(String username,
                               String email,
                               String firstName,
                               String lastName,
                               String password,
                               String roleName) {
        var users = keycloak.realm(targetRealm).users();
        var existing = users.searchByUsername(username, true);
        String userId;

        if (existing == null || existing.isEmpty()) {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            try (Response response = users.create(user)) {
                int status = response.getStatus();
                if (status < 200 || status >= 300) {
                    String body = readResponseBody(response);
                    throw new IllegalStateException("Failed to create user '" + username
                            + "' in realm '" + targetRealm + "' (HTTP " + status + ")"
                            + (body == null || body.isBlank() ? "" : ", body=" + body));
                }
                userId = CreatedResponseUtil.getCreatedId(response);
            }

            resetPassword(users, userId, password);
            assignRealmRoleIfMissing(userId, roleName);
            return true;
        }

        userId = existing.getFirst().getId();
        assignRealmRoleIfMissing(userId, roleName);
        return false;
    }

    private void resetPassword(org.keycloak.admin.client.resource.UsersResource users,
                               String userId,
                               String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);
        users.get(userId).resetPassword(credential);
    }

    private void assignRealmRoleIfMissing(String userId, String roleName) {
        var userResource = keycloak.realm(targetRealm).users().get(userId);
        boolean alreadyAssigned = userResource.roles().realmLevel().listAll().stream()
                .anyMatch(role -> roleName.equals(role.getName()));
        if (alreadyAssigned) {
            return;
        }

        RoleRepresentation role = keycloak.realm(targetRealm).roles().get(roleName).toRepresentation();
        userResource.roles().realmLevel().add(List.of(role));
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

