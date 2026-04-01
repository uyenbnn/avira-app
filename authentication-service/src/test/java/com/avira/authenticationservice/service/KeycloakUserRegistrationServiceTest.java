package com.avira.authenticationservice.service;

import com.avira.authenticationservice.dto.RegisterRequest;
import com.avira.authenticationservice.dto.UserResponse;
import com.avira.commonlib.config.properties.ApplicationProperties;
import com.avira.commonlib.config.properties.KeycloakAuthProperties;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.UserDomainActions;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.user.UserRegisteredEvent;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakUserRegistrationServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private KeycloakAuthProperties keycloakAuthProperties;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    @InjectMocks
    private KeycloakUserRegistrationService keycloakUserRegistrationService;

    @Test
    void shouldPublishUserDomainRegisteredEvent() {
        RegisterRequest request = new RegisterRequest("alice", "alice@avira.com", "StrongPass123", "Alice", "Smith");
        Response response = Response.created(URI.create("http://localhost/users/kc-user-id")).build();
        RoleRepresentation userRole = new RoleRepresentation();
        userRole.setName("USER");

        when(keycloakAuthProperties.getRealm()).thenReturn("avira");
        when(applicationProperties.getName()).thenReturn("authentication-service");

        when(keycloak.realm("avira")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(response);
        when(usersResource.get("kc-user-id")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(userRole);

        UserResponse result = keycloakUserRegistrationService.register(request);

        assertThat(result.id()).isEqualTo("kc-user-id");
        verify(userResource).resetPassword(any());
        verify(roleScopeResource).add(List.of(userRole));
        verify(eventPublisher).publish(
                eq(EventTopics.USER_DOMAIN),
                eq(UserDomainActions.REGISTERED),
                eq("authentication-service"),
                eq("kc-user-id"),
                eq("kc-user-id"),
                eq(new UserRegisteredEvent("kc-user-id", "alice", "alice@avira.com", "Alice", "Smith")),
                eq(Map.of())
        );
    }
}

