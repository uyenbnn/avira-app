package com.avira.userservice.client;

import com.avira.userservice.dto.TokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAuthClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${keycloak.sync.server-url}")
    private String serverUrl;

    @Value("${keycloak.auth.realm:${keycloak.sync.realm}}")
    private String realm;

    @Value("${keycloak.auth.client-id:${keycloak.sync.realm}-user-client}")
    private String clientId;

    @Value("${keycloak.auth.client-secret:}")
    private String clientSecret;

    private RestClient restClient;

    @PostConstruct
    void init() {
        restClient = restClientBuilder.build();
    }

    public TokenResponse login(String username, String password) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        // Public clients must NOT send client_secret; confidential clients MUST.
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
        form.add("username", username);
        form.add("password", password);

        log.debug("Requesting token from {} for client_id={}", tokenUrl, clientId);

        try {
            TokenResponse response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);

            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new IllegalStateException("Keycloak returned an empty access token");
            }
            return response;
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            String body = ex.getResponseBodyAsString();
            log.error("Keycloak token request failed: status={}, client_id={}, body={}", status, clientId, body);
            if (status == 400 || status == 401) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            throw new IllegalStateException("Login failed with Keycloak (HTTP " + status + "): " + body, ex);
        }
    }
}
