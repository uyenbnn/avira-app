package com.avira.applicationservice.authentication.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.avira.applicationservice.authentication.AuthMode;
import com.avira.applicationservice.authentication.dto.AppTokenResponse;
import com.avira.applicationservice.authentication.dto.TokenExchangeRequest;
import com.avira.applicationservice.authentication.service.TokenExchangeService;

class AppAuthenticationControllerTest {

    private MockMvc mockMvc;

    private TokenExchangeService tokenExchangeService;

        @BeforeEach
        void setUp() {
                tokenExchangeService = mock(TokenExchangeService.class);
                AppAuthenticationController controller = new AppAuthenticationController(tokenExchangeService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

    @Test
    void shouldExchangeTokenUsingTenantFromHeader() throws Exception {
        when(tokenExchangeService.exchange(eq("tenant-1"), eq("app-1"), eq(new TokenExchangeRequest("subject", AuthMode.KEYCLOAK))))
                .thenReturn(new AppTokenResponse("a", "r", 300, "Bearer", "app-1", "tenant-1", "KEYCLOAK"));

        String requestBody = """
                {
                  "subjectToken": "subject",
                  "authMode": "KEYCLOAK"
                }
                """;

        mockMvc.perform(post("/api/apps/app-1/auth/token-exchange")
                        .header("X-Tenant-Id", "tenant-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.strategy").value("KEYCLOAK"));
    }

    @Test
    void shouldRejectWhenTenantHeaderIsMissing() throws Exception {
        String requestBody = """
                {
                  "subjectToken": "subject",
                  "authMode": "KEYCLOAK"
                }
                """;

        mockMvc.perform(post("/api/apps/app-1/auth/token-exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Missing tenant context from validated token"));
    }
}
