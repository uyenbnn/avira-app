package com.avira.authenticationservice.service;

import com.avira.authenticationservice.dto.UserRolesResponse;
import com.avira.commonlib.constants.UserRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserRoleService {

    private final Keycloak keycloak;

    @Value("${keycloak.auth.realm:avira}")
    private String realm;

    /**
     * Replaces the managed roles of a Keycloak user.
     * Only roles in {@link UserRoles#ALL} are touched — Keycloak system roles are never modified.
     *
     * @param userId Keycloak user UUID
     * @param requestedRoles the full desired set of managed roles for the user
     * @return the resulting managed roles after update
     */
    public UserRolesResponse updateRoles(String userId, Set<String> requestedRoles) {
        validate(requestedRoles);

        var userRoles = keycloak.realm(realm).users().get(userId).roles().realmLevel();

        Set<String> currentManagedRoles = userRoles.listAll().stream()
                .map(RoleRepresentation::getName)
                .filter(UserRoles.ALL::contains)
                .collect(Collectors.toSet());

        List<RoleRepresentation> toRemove = currentManagedRoles.stream()
                .filter(role -> !requestedRoles.contains(role))
                .map(this::fetchRole)
                .toList();

        List<RoleRepresentation> toAdd = requestedRoles.stream()
                .filter(role -> !currentManagedRoles.contains(role))
                .map(this::fetchRole)
                .toList();

        if (!toRemove.isEmpty()) {
            userRoles.remove(toRemove);
        }
        if (!toAdd.isEmpty()) {
            userRoles.add(toAdd);
        }

        log.info("Updated roles for user '{}' in realm '{}': removed={}, added={}",
                userId, realm,
                toRemove.stream().map(RoleRepresentation::getName).toList(),
                toAdd.stream().map(RoleRepresentation::getName).toList());

        return new UserRolesResponse(userId, Set.copyOf(requestedRoles));
    }

    private void validate(Set<String> roles) {
        Set<String> invalid = roles.stream()
                .filter(role -> !UserRoles.ALL.contains(role))
                .collect(Collectors.toSet());
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid roles: " + invalid + ". Allowed: " + UserRoles.ALL);
        }
    }

    private RoleRepresentation fetchRole(String roleName) {
        return keycloak.realm(realm).roles().get(roleName).toRepresentation();
    }
}

