package com.avira.iamservice.realm.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.avira.iamservice.realm.config.IamRealmProperties;
import com.avira.iamservice.realm.config.KeycloakInitProperties;

@Service
public class RestKeycloakRealmProvisioningService implements KeycloakRealmProvisioningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestKeycloakRealmProvisioningService.class);
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_OF_MAPS =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;
    private final KeycloakInitProperties keycloakInitProperties;
    private final IamRealmProperties iamRealmProperties;

    public RestKeycloakRealmProvisioningService(
            RestClient.Builder restClientBuilder,
            KeycloakInitProperties keycloakInitProperties,
            IamRealmProperties iamRealmProperties
    ) {
        this.restClient = restClientBuilder
                .baseUrl(trimTrailingSlash(keycloakInitProperties.baseUrl()))
                .build();
        this.keycloakInitProperties = keycloakInitProperties;
        this.iamRealmProperties = iamRealmProperties;
    }

    @Override
    public boolean initSharedRealm(String realmName) {
        String adminToken = obtainAdminAccessToken();
        if (realmExists(adminToken, realmName)) {
            return false;
        }

        try {
            restClient.post()
                    .uri("/admin/realms")
                    .header(AUTHORIZATION, BEARER_PREFIX + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("realm", realmName, "enabled", true))
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("Created shared realm {}", realmName);
            return true;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public TenantClientProvisionResult provisionTenantClient(String tenantId, String realmName) {
        String adminToken = obtainAdminAccessToken();
        String clientId = iamRealmProperties.tenantBackendClientId(tenantId);

        if (findClientUuid(adminToken, realmName, clientId).isPresent()) {
            return new TenantClientProvisionResult(clientId, "ALREADY_EXISTS");
        }

        try {
            restClient.post()
                    .uri("/admin/realms/{realmName}/clients", realmName)
                    .header(AUTHORIZATION, BEARER_PREFIX + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "clientId", clientId,
                            "enabled", true,
                            "protocol", "openid-connect",
                            "publicClient", false,
                            "serviceAccountsEnabled", true,
                            "directAccessGrantsEnabled", false,
                            "standardFlowEnabled", false
                    ))
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("Provisioned tenant backend client {} in realm {}", clientId, realmName);
            return new TenantClientProvisionResult(clientId, "PROVISIONED");
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                return new TenantClientProvisionResult(clientId, "ALREADY_EXISTS");
            }
            throw ex;
        }
    }

    private Optional<String> findClientUuid(String adminToken, String realmName, String clientId) {
        List<Map<String, Object>> clients = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/{realmName}/clients")
                        .queryParam("clientId", clientId)
                        .build(realmName))
                .header(AUTHORIZATION, BEARER_PREFIX + adminToken)
                .retrieve()
                .body(LIST_OF_MAPS);

        if (clients == null || clients.isEmpty()) {
            return Optional.empty();
        }

        Object value = clients.getFirst().get("id");
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.toString());
    }

    private boolean realmExists(String adminToken, String realmName) {
        try {
            restClient.get()
                    .uri("/admin/realms/{realmName}", realmName)
                    .header(AUTHORIZATION, BEARER_PREFIX + adminToken)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw ex;
        }
    }

    private String obtainAdminAccessToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloakInitProperties.adminClientId());
        formData.add("username", keycloakInitProperties.adminUsername());
        formData.add("password", keycloakInitProperties.adminPassword());

        Map<String, Object> response = restClient.post()
                .uri("/realms/{realmName}/protocol/openid-connect/token", keycloakInitProperties.adminRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(MAP_TYPE);

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Keycloak token response does not contain access_token");
        }
        return response.get("access_token").toString();
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("iam.init.keycloak.base-url must not be blank");
        }
        String trimmed = value.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}