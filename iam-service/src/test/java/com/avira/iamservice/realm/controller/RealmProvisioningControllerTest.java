package com.avira.iamservice.realm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.avira.iamservice.realm.IdentityMode;
import com.avira.iamservice.realm.dto.RealmResolutionResponse;
import com.avira.iamservice.realm.dto.SharedRealmInitResponse;
import com.avira.iamservice.realm.dto.TenantProvisionRequest;
import com.avira.iamservice.realm.dto.TenantProvisionResponse;
import com.avira.iamservice.realm.dto.TenantRealmConfigResponse;
import com.avira.iamservice.realm.service.RealmProvisioningService;

class RealmProvisioningControllerTest {

    private MockMvc mockMvc;

    private RealmProvisioningService realmProvisioningService;

        @BeforeEach
        void setUp() {
                realmProvisioningService = mock(RealmProvisioningService.class);
                RealmProvisioningController controller = new RealmProvisioningController(realmProvisioningService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

    @Test
    void shouldInitSharedRealm() throws Exception {
        when(realmProvisioningService.initSharedRealm())
                .thenReturn(new SharedRealmInitResponse("avira-platform", "INITIALIZED"));

        mockMvc.perform(post("/api/iam/internal/init/realms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realm").value("avira-platform"));
    }

    @Test
    void shouldProvisionTenant() throws Exception {
        when(realmProvisioningService.provisionTenant(eq("tenant-1"), any(TenantProvisionRequest.class)))
                .thenReturn(new TenantProvisionResponse("tenant-1", "avira-platform", "tenant-tenant-1-client", "PROVISIONED"));

        String requestBody = """
                {
                  "tenantId": "tenant-1",
                  "tenantName": "Acme",
                  "contactEmail": "ops@acme.dev",
                  "identityMode": "SHARED_REALM"
                }
                """;

        mockMvc.perform(post("/api/iam/internal/init/tenants/tenant-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROVISIONED"));
    }

    @Test
    void shouldRejectDedicatedRealmInMvp() throws Exception {
        when(realmProvisioningService.upsertTenantRealmConfig(any()))
                .thenThrow(new IllegalArgumentException("MVP only supports SHARED_REALM"));

        String requestBody = """
                {
                  "tenantId": "4e30af29-5f69-40c7-a0da-39f319f42f5d",
                  "identityMode": "DEDICATED_REALM"
                }
                """;

        mockMvc.perform(post("/api/iam/client/tenant-realm-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("MVP only supports SHARED_REALM"));
    }

    @Test
    void shouldResolveRealm() throws Exception {
        when(realmProvisioningService.resolveRealm("tenant-1"))
                .thenReturn(new RealmResolutionResponse("tenant-1", "avira-platform", IdentityMode.SHARED_REALM));

        mockMvc.perform(get("/api/iam/client/realms/tenants/tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realm").value("avira-platform"));
    }

    @Test
    void shouldUpsertRealmConfig() throws Exception {
        when(realmProvisioningService.upsertTenantRealmConfig(any()))
                .thenReturn(new TenantRealmConfigResponse("tenant-1", IdentityMode.SHARED_REALM, false));

        String requestBody = """
                {
                  "tenantId": "tenant-1",
                  "identityMode": "SHARED_REALM"
                }
                """;

        mockMvc.perform(post("/api/iam/client/tenant-realm-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dedicatedRealmApproved").value(false));
    }
}
