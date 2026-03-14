package com.avira.userservice.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakRealmBootstrapServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmsResource realmsResource;

    @Mock
    private ApplicationArguments args;

    @Test
    void shouldIncludeHttp401DetailsInFailFastException() {
        KeycloakRealmBootstrapService service = new KeycloakRealmBootstrapService(keycloak);
        setProps(service, true, true);

        Response unauthorized = Response.status(401)
                .entity("{\"error\":\"unauthorized\"}")
                .build();

        when(keycloak.realms()).thenReturn(realmsResource);
        when(realmsResource.findAll()).thenThrow(new WebApplicationException(unauthorized));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.run(args));

        assertThat(ex.getMessage()).contains("httpStatus=401");
        assertThat(ex.getMessage()).contains("targetRealm=avira");
        assertThat(ex.getMessage()).contains("clientId=admin-cli");
        assertThat(ex.getMessage()).contains("responseBody={\"error\":\"unauthorized\"}");
    }

    @Test
    void shouldNotThrowWhenFailFastDisabled() {
        KeycloakRealmBootstrapService service = new KeycloakRealmBootstrapService(keycloak);
        setProps(service, true, false);

        when(keycloak.realms()).thenReturn(realmsResource);
        when(realmsResource.findAll()).thenThrow(new RuntimeException("network down"));

        assertDoesNotThrow(() -> service.run(args));
    }

    @Test
    void shouldSkipCreateWhenRealmAlreadyExists() {
        KeycloakRealmBootstrapService service = new KeycloakRealmBootstrapService(keycloak);
        setProps(service, true, true);

        RealmRepresentation existing = new RealmRepresentation();
        existing.setRealm("avira");

        when(keycloak.realms()).thenReturn(realmsResource);
        when(realmsResource.findAll()).thenReturn(List.of(existing));

        service.run(args);

        verify(realmsResource, never()).create(any(RealmRepresentation.class));
    }

    private static void setProps(KeycloakRealmBootstrapService service,
                                 boolean autoCreate,
                                 boolean failFast) {
        ReflectionTestUtils.setField(service, "serverUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "adminRealm", "master");
        ReflectionTestUtils.setField(service, "clientId", "admin-cli");
        ReflectionTestUtils.setField(service, "targetRealm", "avira");
        ReflectionTestUtils.setField(service, "autoCreateRealm", autoCreate);
        ReflectionTestUtils.setField(service, "failFast", failFast);
    }
}

