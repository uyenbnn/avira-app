package com.avira.iamservice.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.avira.iamservice.auth.dto.LoginRequest;
import com.avira.iamservice.auth.dto.LogoutRequest;
import com.avira.iamservice.auth.dto.RefreshRequest;
import com.avira.iamservice.auth.dto.TokenResponse;
import com.avira.iamservice.auth.service.AuthService;

class AuthControllerTest {

    private MockMvc mockMvc;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldLogin() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("access-token", "refresh-token", 3600L, "Bearer"));

        String requestBody = """
                {
                  "tenantId": "tenant-1",
                  "username": "owner@acme.dev",
                  "password": "secret",
                  "appId": "app-1"
                }
                """;

        mockMvc.perform(post("/api/iam/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRefresh() throws Exception {
        when(authService.refresh(any(RefreshRequest.class)))
                .thenReturn(new TokenResponse("new-access", "new-refresh", 3600L, "Bearer"));

        String requestBody = """
                {
                  "tenantId": "tenant-1",
                  "refreshToken": "refresh-token"
                }
                """;

        mockMvc.perform(post("/api/iam/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void shouldLogout() throws Exception {
        doNothing().when(authService).logout(any(LogoutRequest.class));

        String requestBody = """
                {
                  "tenantId": "tenant-1",
                  "refreshToken": "refresh-token"
                }
                """;

        mockMvc.perform(post("/api/iam/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequestWhenLoginPayloadIsInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("tenantId is required"));

        String requestBody = """
                {
                  "tenantId": "",
                  "username": "owner@acme.dev",
                  "password": "secret"
                }
                """;

        mockMvc.perform(post("/api/iam/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("tenantId is required"));
    }

    @Test
    void shouldReturnBadRequestWhenRefreshTokenIsInvalid() throws Exception {
        when(authService.refresh(any(RefreshRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        String requestBody = """
                {
                  "tenantId": "tenant-1",
                  "refreshToken": "bad-token"
                }
                """;

        mockMvc.perform(post("/api/iam/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void shouldReturnBadRequestWhenLogoutPayloadInvalid() throws Exception {
        doThrow(new IllegalArgumentException("refreshToken is required"))
                .when(authService)
                .logout(any(LogoutRequest.class));

        String requestBody = """
                {
                  "tenantId": "tenant-1",
                  "refreshToken": ""
                }
                """;

        mockMvc.perform(post("/api/iam/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("refreshToken is required"));
    }
}
