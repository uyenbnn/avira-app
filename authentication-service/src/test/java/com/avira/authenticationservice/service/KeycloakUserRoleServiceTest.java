package com.avira.authenticationservice.service;

import com.avira.authenticationservice.dto.UserRolesResponse;
import com.avira.commonlib.constants.UserRoles;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakUserRoleServiceTest {

    @Mock private Keycloak keycloak;
    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private UserResource userResource;
    @Mock private RoleMappingResource roleMappingResource;
    @Mock private RoleScopeResource roleScopeResource;
    @Mock private RolesResource rolesResource;

    private static final String USER_ID = "user-uuid-1";
    private static final String REALM    = "avira";

    private KeycloakUserRoleService service() {
        KeycloakUserRoleService svc = new KeycloakUserRoleService(keycloak);
        ReflectionTestUtils.setField(svc, "realm", REALM);
        return svc;
    }

    private void stubUserRoles(List<RoleRepresentation> existing) {
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(roleScopeResource.listAll()).thenReturn(existing);
    }

    private RoleRepresentation role(String name) {
        RoleRepresentation r = new RoleRepresentation();
        r.setName(name);
        return r;
    }

    @Test
    void shouldAddNewRolesAndRemoveDroppedOnes() {
        // User currently has USER + SELLER; we want USER + BUYER
        stubUserRoles(List.of(role(UserRoles.USER), role(UserRoles.SELLER)));
        when(realmResource.roles()).thenReturn(rolesResource);
        RoleResource buyerRoleResource = mock(RoleResource.class);
        when(rolesResource.get(UserRoles.BUYER)).thenReturn(buyerRoleResource);
        when(buyerRoleResource.toRepresentation()).thenReturn(role(UserRoles.BUYER));
        RoleResource sellerRoleResource = mock(RoleResource.class);
        when(rolesResource.get(UserRoles.SELLER)).thenReturn(sellerRoleResource);
        when(sellerRoleResource.toRepresentation()).thenReturn(role(UserRoles.SELLER));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleRepresentation>> addCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleRepresentation>> removeCaptor = ArgumentCaptor.forClass(List.class);

        UserRolesResponse response = service().updateRoles(USER_ID, Set.of(UserRoles.USER, UserRoles.BUYER));

        verify(roleScopeResource).remove(removeCaptor.capture());
        verify(roleScopeResource).add(addCaptor.capture());

        assertThat(removeCaptor.getValue()).extracting(RoleRepresentation::getName)
                .containsExactly(UserRoles.SELLER);
        assertThat(addCaptor.getValue()).extracting(RoleRepresentation::getName)
                .containsExactly(UserRoles.BUYER);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.roles()).containsExactlyInAnyOrder(UserRoles.USER, UserRoles.BUYER);
    }

    @Test
    void shouldNotCallAddOrRemoveWhenRolesUnchanged() {
        stubUserRoles(List.of(role(UserRoles.USER)));

        service().updateRoles(USER_ID, Set.of(UserRoles.USER));

        verify(roleScopeResource, never()).add(anyList());
        verify(roleScopeResource, never()).remove(anyList());
    }

    @Test
    void shouldRejectUnknownRoles() {
        assertThatThrownBy(() -> service().updateRoles(USER_ID, Set.of("SUPERUSER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid roles")
                .hasMessageContaining("SUPERUSER");
    }
}



