package com.avira.applicationinitializationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakInitializationServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmsResource realmsResource;

    @Mock
    private RealmResource realmResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private ClientsResource clientsResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Test
    void shouldSkipRecreateWhenAllSeedDataAlreadyExists() {
        KeycloakInitializationService service = new KeycloakInitializationService(keycloak);
        setProps(service);

        RealmRepresentation existingRealm = new RealmRepresentation();
        existingRealm.setRealm("avira");

        RoleRepresentation adminRole = new RoleRepresentation();
        adminRole.setName("ADMIN");
        RoleRepresentation anonymousRole = new RoleRepresentation();
        anonymousRole.setName("ANONYMOUS");

        UserRepresentation existingAnonymous = new UserRepresentation();
        existingAnonymous.setId("anon-id");
        UserRepresentation existingAdmin = new UserRepresentation();
        existingAdmin.setId("admin-id");

        when(keycloak.realms()).thenReturn(realmsResource);
        when(realmsResource.findAll()).thenReturn(List.of(existingRealm));
        when(keycloak.realm("avira")).thenReturn(realmResource);

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.list()).thenReturn(List.of(
                role("USER"), role("ADMIN"), role("SELLER"), role("BUYER"), role("ANONYMOUS")
        ));

        when(realmResource.clients()).thenReturn(clientsResource);
        when(clientsResource.findByClientId(anyString())).thenReturn(List.of(new ClientRepresentation()));

        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("anonymous", true)).thenReturn(List.of(existingAnonymous));
        when(usersResource.searchByUsername("avira-admin", true)).thenReturn(List.of(existingAdmin));

        when(usersResource.get("anon-id")).thenReturn(userResource);
        when(usersResource.get("admin-id")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(roleScopeResource.listAll()).thenReturn(List.of(adminRole, anonymousRole));

        var response = service.initializeKeycloak();

        assertThat(response.realmCreated()).isFalse();
        assertThat(response.userClientCreated()).isFalse();
        assertThat(response.adminClientCreated()).isFalse();
        assertThat(response.anonymousUserCreated()).isFalse();
        assertThat(response.defaultAdminUserCreated()).isFalse();

        verify(realmsResource, never()).create(org.mockito.ArgumentMatchers.any(RealmRepresentation.class));
    }

    private static RoleRepresentation role(String name) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(name);
        return role;
    }

    private static void setProps(KeycloakInitializationService service) {
        ReflectionTestUtils.setField(service, "targetRealm", "avira");

        ReflectionTestUtils.setField(service, "anonymousUsername", "anonymous");
        ReflectionTestUtils.setField(service, "anonymousEmail", "anonymous@avira.local");
        ReflectionTestUtils.setField(service, "anonymousPassword", "anonymous123");

        ReflectionTestUtils.setField(service, "defaultAdminUsername", "avira-admin");
        ReflectionTestUtils.setField(service, "defaultAdminEmail", "admin@avira.local");
        ReflectionTestUtils.setField(service, "defaultAdminPassword", "admin123");
    }
}

