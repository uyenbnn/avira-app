package com.avira.platformservice.mvp.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

import com.avira.platformservice.mvp.ApplicationStatus;
import com.avira.platformservice.mvp.AuthMode;
import com.avira.platformservice.mvp.IdentityMode;
import com.avira.platformservice.mvp.TenantStatus;
import com.avira.platformservice.mvp.dto.ApplicationResponse;
import com.avira.platformservice.mvp.dto.TenantResponse;
import com.avira.platformservice.mvp.service.PlatformMvpService;

class PlatformMvpControllerTest {

    private MockMvc mockMvc;

    private PlatformMvpService platformMvpService;

        @BeforeEach
        void setUp() {
                platformMvpService = mock(PlatformMvpService.class);
                PlatformMvpController controller = new PlatformMvpController(platformMvpService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

    @Test
    void shouldCreateTenant() throws Exception {
        when(platformMvpService.createTenant(any())).thenReturn(new TenantResponse(
                "tenant-1",
                "Acme",
                "ops@acme.dev",
                IdentityMode.SHARED_REALM,
                TenantStatus.ACTIVE,
                Instant.parse("2026-04-11T00:00:00Z")
        ));

        String requestBody = """
                {
                  "name": "Acme",
                  "contactEmail": "ops@acme.dev"
                }
                """;

        mockMvc.perform(post("/api/platform/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identityMode").value("SHARED_REALM"));
    }

    @Test
    void shouldBlockCrossTenantAccess() throws Exception {
        mockMvc.perform(get("/api/platform/tenants/tenant-1")
                        .header("X-Tenant-Id", "tenant-2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Cross-tenant access is not allowed"));
    }

    @Test
    void shouldListApplicationsForOwnedTenant() throws Exception {
        when(platformMvpService.listApplications(eq("tenant-1"))).thenReturn(List.of(new ApplicationResponse(
                "app-1",
                "tenant-1",
                "Store",
                "store.acme.dev",
                AuthMode.KEYCLOAK,
                ApplicationStatus.ACTIVE,
                Map.of(),
                Instant.parse("2026-04-11T00:00:00Z")
        )));

        mockMvc.perform(get("/api/platform/tenants/tenant-1/applications")
                        .header("X-Tenant-Id", "tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appId").value("app-1"));
    }
}
