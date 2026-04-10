package com.avira.iamservice.initservice.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.avira.iamservice.initservice.config.KeycloakInitProperties;

@Service
public class KeycloakProvisionService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private final WebClient keycloakAdminWebClient;
    private final KeycloakInitProperties keycloakInitProperties;

    public KeycloakProvisionService(
            @Qualifier("keycloakAdminWebClient") WebClient keycloakAdminWebClient,
            KeycloakInitProperties keycloakInitProperties
    ) {
        this.keycloakAdminWebClient = keycloakAdminWebClient;
        this.keycloakInitProperties = keycloakInitProperties;
    }

    public void provisionSharedRealm() {
        provisionRealm(keycloakInitProperties.getSharedRealm());
    }

    public void provisionTenantRealm(String tenantId) {
        String dedicatedRealm = keycloakInitProperties.getDedicatedPrefix() + tenantId;
        provisionRealm(dedicatedRealm);
    }

    public void ensureRealmProvisioned(String realmName) {
        provisionRealm(realmName);
    }

    private void provisionRealm(String realmName) {
        String accessToken = obtainAdminAccessToken();
        ensureRealmExists(accessToken, realmName);
        ensureClientExists(accessToken, realmName, keycloakInitProperties.getFrontendClientId(), true, null);
        ensureClientExists(
                accessToken,
                realmName,
                keycloakInitProperties.getBackendClientId(),
                false,
                keycloakInitProperties.getBackendClientSecret()
        );
        ensureRoleExists(accessToken, realmName, ROLE_ADMIN);
        ensureRoleExists(accessToken, realmName, ROLE_USER);
    }

    private String obtainAdminAccessToken() {
        MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
        tokenBody.add("grant_type", "password");
        tokenBody.add("client_id", keycloakInitProperties.getAdminClientId());
        tokenBody.add("username", keycloakInitProperties.getAdminUsername());
        tokenBody.add("password", keycloakInitProperties.getAdminPassword());

        Map<String, Object> response = keycloakAdminWebClient.post()
            .uri(baseUrl() + "/realms/" + keycloakInitProperties.getAdminRealm() + "/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(tokenBody))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
            })
            .block();

        Object accessToken = response == null ? null : response.get("access_token");
        if (accessToken == null || accessToken.toString().isBlank()) {
            throw new IllegalStateException("Unable to obtain Keycloak admin access token");
        }
        return accessToken.toString();
    }

    private void ensureRealmExists(String accessToken, String realmName) {
        try {
            keycloakAdminWebClient.get()
                .uri(adminRealmUrl(realmName))
                .headers(headers -> headers.addAll(adminHeaders(accessToken)))
                .retrieve()
                .toBodilessEntity()
                .block();
            return;
        } catch (WebClientResponseException.NotFound notFound) {
            // Create realm when missing.
        }

        Map<String, Object> realmPayload = Map.of(
                "realm", realmName,
                "enabled", Boolean.TRUE
        );

        keycloakAdminWebClient.post()
            .uri(adminRealmsUrl())
            .headers(headers -> headers.addAll(adminHeaders(accessToken)))
            .bodyValue(realmPayload)
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    private void ensureClientExists(
            String accessToken,
            String realmName,
            String clientId,
            boolean publicClient,
            String clientSecret
    ) {
        String clientsUrl = adminRealmUrl(realmName) + "/clients";
            List<Map<String, Object>> existingClients = keycloakAdminWebClient.get()
                .uri(clientsUrl + "?clientId=" + clientId)
                .headers(headers -> headers.addAll(adminHeaders(accessToken)))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                })
                .block();

        if (existingClients == null || existingClients.isEmpty()) {
            Map<String, Object> payload = Map.of(
                    "clientId", clientId,
                    "enabled", Boolean.TRUE,
                    "publicClient", publicClient,
                    "directAccessGrantsEnabled", Boolean.TRUE,
                    "serviceAccountsEnabled", !publicClient,
                    "standardFlowEnabled", Boolean.TRUE,
                    "secret", publicClient ? "" : clientSecret
            );

                keycloakAdminWebClient.post()
                    .uri(clientsUrl)
                    .headers(headers -> headers.addAll(adminHeaders(accessToken)))
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return;
        }

        if (publicClient) {
            return;
        }

            Object id = existingClients.get(0).get("id");
        if (id == null) {
            return;
        }

        Map<String, Object> updatePayload = Map.of(
                "id", id,
                "clientId", clientId,
                "enabled", Boolean.TRUE,
                "publicClient", Boolean.FALSE,
                "secret", clientSecret,
                "serviceAccountsEnabled", Boolean.TRUE,
                "directAccessGrantsEnabled", Boolean.TRUE,
                "standardFlowEnabled", Boolean.TRUE
        );

            keycloakAdminWebClient.put()
                .uri(clientsUrl + "/" + id)
                .headers(headers -> headers.addAll(adminHeaders(accessToken)))
                .bodyValue(updatePayload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void ensureRoleExists(String accessToken, String realmName, String roleName) {
        try {
                keycloakAdminWebClient.get()
                    .uri(adminRealmUrl(realmName) + "/roles/" + roleName)
                    .headers(headers -> headers.addAll(adminHeaders(accessToken)))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();
            return;
            } catch (WebClientResponseException.NotFound notFound) {
            // Create role when missing.
        }

        Map<String, Object> rolePayload = Map.of(
                "name", roleName,
                "description", "Provisioned by iam-service"
        );

            keycloakAdminWebClient.post()
                .uri(adminRealmUrl(realmName) + "/roles")
                .headers(headers -> headers.addAll(adminHeaders(accessToken)))
                .bodyValue(rolePayload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private HttpHeaders adminHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String baseUrl() {
        String configured = keycloakInitProperties.getBaseUrl();
        return configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
    }

    private String adminRealmsUrl() {
        return baseUrl() + "/admin/realms";
    }

    private String adminRealmUrl(String realmName) {
        return adminRealmsUrl() + "/" + realmName;
    }
}
