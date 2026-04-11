package com.avira.iamservice.realm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avira.iamservice.realm.IdentityMode;
import com.avira.iamservice.realm.TenantRealmConfig;
import com.avira.iamservice.realm.config.IamRealmProperties;

class SharedOrDedicatedRealmResolverTest {

    private InMemoryTenantRealmConfigStore store;
    private SharedOrDedicatedRealmResolver resolver;

    @BeforeEach
    void setUp() {
        store = new InMemoryTenantRealmConfigStore();
        resolver = new SharedOrDedicatedRealmResolver(store, new IamRealmProperties("avira-platform", "tenant_"));
    }

    @Test
    void shouldResolveSharedRealmForConfiguredTenant() {
        String tenantId = "4e30af29-5f69-40c7-a0da-39f319f42f5d";
        store.upsert(new TenantRealmConfig(tenantId, IdentityMode.SHARED_REALM, false));

        String resolvedRealm = resolver.resolveRealm(tenantId);

        assertEquals("avira-platform", resolvedRealm);
    }

    @Test
    void shouldFallbackToPrefixedRealmForNonUuidTenantId() {
        assertEquals("tenant_abc", resolver.resolveRealm("abc"));
    }

    @Test
    void shouldFailFastWhenUuidTenantHasNoConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolveRealm("4e30af29-5f69-40c7-a0da-39f319f42f5d"));
    }
}
