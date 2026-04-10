package com.avira.iamservice.initservice.realm;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.avira.iamservice.clientservice.config.IamRealmProperties;
import com.avira.iamservice.clientservice.domain.IdentityMode;
import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.clientservice.repository.TenantRealmConfigRepository;

@ExtendWith(MockitoExtension.class)
class SharedOrDedicatedRealmResolverTest {

    @Mock
    private TenantRealmConfigRepository repository;

    private SharedOrDedicatedRealmResolver newResolver() {
        IamRealmProperties properties = new IamRealmProperties();
        properties.setSharedName("avira-platform");
        properties.setDedicatedPrefix("tenant_");
        return new SharedOrDedicatedRealmResolver(repository, properties);
    }

    @Test
    void shouldResolveSharedRealmFromTenantConfig() {
        UUID tenantId = UUID.randomUUID();
        TenantRealmConfig config = new TenantRealmConfig(tenantId, IdentityMode.SHARED_REALM, "avira-platform", false);
        when(repository.findById(tenantId)).thenReturn(Optional.of(config));

        assertEquals("avira-platform", newResolver().resolveRealm(tenantId.toString()));
    }

    @Test
    void shouldResolveDedicatedRealmFromTenantConfig() {
        UUID tenantId = UUID.randomUUID();
        TenantRealmConfig config = new TenantRealmConfig(tenantId, IdentityMode.DEDICATED_REALM, "ignored", true);
        when(repository.findById(tenantId)).thenReturn(Optional.of(config));

        assertEquals("tenant_" + tenantId, newResolver().resolveRealm(tenantId.toString()));
    }

    @Test
    void shouldUseDedicatedPrefixWhenTenantIdIsNonUuid() {
        assertEquals("tenant_default-saas", newResolver().resolveRealm("default-saas"));
    }

    @Test
    void shouldThrowWhenTenantConfigMissingForUuid() {
        UUID tenantId = UUID.randomUUID();
        when(repository.findById(tenantId)).thenReturn(Optional.empty());

        Throwable thrown = assertThrows(IllegalArgumentException.class, () -> newResolver().resolveRealm(tenantId.toString()));
        assertEquals(IllegalArgumentException.class, thrown.getClass());
    }
}
