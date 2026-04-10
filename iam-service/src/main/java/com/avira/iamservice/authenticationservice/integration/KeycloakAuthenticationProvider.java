package com.avira.iamservice.authenticationservice.integration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.avira.iamservice.authenticationservice.config.AuthenticationProperties;
import com.avira.iamservice.authenticationservice.dto.TokenResponse;
import com.avira.iamservice.initservice.realm.RealmResolver;

@Component
@ConditionalOnProperty(prefix = "iam.auth", name = "provider", havingValue = "keycloak", matchIfMissing = true)
public class KeycloakAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate keycloakRestTemplate;
    private final AuthenticationProperties authenticationProperties;
    private final RealmResolver realmResolver;

    public KeycloakAuthenticationProvider(
            RestTemplate keycloakRestTemplate,
            AuthenticationProperties authenticationProperties,
            RealmResolver realmResolver
    ) {
        this.keycloakRestTemplate = keycloakRestTemplate;
        this.authenticationProperties = authenticationProperties;
        this.realmResolver = realmResolver;
    }

    @Override
    public TokenResponse login(String tenantId, String username, String password) {
        String realm = realmResolver.resolveRealm(tenantId);
        Map<String, Object> tokenPayload = tokenRequest(realm, tokenRequestBody(Map.of(
                "grant_type", "password",
                "client_id", authenticationProperties.getFrontendClientId(),
                "username", username,
                "password", password
        )));

        return toTokenResponse(tokenPayload, tenantId, "iam-service");
    }

    @Override
    public TokenResponse refresh(String tenantId, String refreshToken) {
        String realm = realmResolver.resolveRealm(tenantId);
        Map<String, Object> tokenPayload = tokenRequest(realm, tokenRequestBody(Map.of(
                "grant_type", "refresh_token",
                "client_id", authenticationProperties.getFrontendClientId(),
                "refresh_token", refreshToken
        )));

        return toTokenResponse(tokenPayload, tenantId, "iam-service");
    }

    @Override
    public Map<String, Object> userInfo(String tenantId, String accessToken) {
        String realm = realmResolver.resolveRealm(tenantId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map<String, Object>> response = keycloakRestTemplate.exchange(
                keycloakBaseUrl() + "/realms/" + realm + "/protocol/openid-connect/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody() == null ? Map.of() : response.getBody();
    }

    @Override
    public boolean introspect(String tenantId, String accessToken) {
        String realm = realmResolver.resolveRealm(tenantId);
        MultiValueMap<String, String> body = tokenRequestBody(Map.of(
                "client_id", authenticationProperties.getBackendClientId(),
                "client_secret", authenticationProperties.getBackendClientSecret(),
                "token", accessToken
        ));

        ResponseEntity<Map<String, Object>> response = keycloakRestTemplate.exchange(
                keycloakBaseUrl() + "/realms/" + realm + "/protocol/openid-connect/token/introspect",
                HttpMethod.POST,
                new HttpEntity<>(body, formHeaders()),
                new ParameterizedTypeReference<>() {
                }
        );

        Object active = response.getBody() == null ? null : response.getBody().get("active");
        return active instanceof Boolean isActive && isActive;
    }

    @Override
    public String clientCredentialsToken(String tenantId) {
        String realm = realmResolver.resolveRealm(tenantId);
        Map<String, Object> response = tokenRequest(realm, tokenRequestBody(Map.of(
                "grant_type", "client_credentials",
                "client_id", authenticationProperties.getBackendClientId(),
                "client_secret", authenticationProperties.getBackendClientSecret()
        )));

        Object accessToken = response.get("access_token");
        return accessToken == null ? "" : accessToken.toString();
    }

    @Override
    public void logout(String tenantId, String refreshToken) {
        String realm = realmResolver.resolveRealm(tenantId);
        MultiValueMap<String, String> body = tokenRequestBody(Map.of(
                "client_id", authenticationProperties.getBackendClientId(),
                "client_secret", authenticationProperties.getBackendClientSecret(),
                "refresh_token", refreshToken
        ));

        keycloakRestTemplate.exchange(
                keycloakBaseUrl() + "/realms/" + realm + "/protocol/openid-connect/logout",
                HttpMethod.POST,
                new HttpEntity<>(body, formHeaders()),
                Void.class
        );
    }

    private Map<String, Object> tokenRequest(String realm, MultiValueMap<String, String> body) {
        ResponseEntity<Map<String, Object>> response = keycloakRestTemplate.exchange(
                keycloakBaseUrl() + "/realms/" + realm + "/protocol/openid-connect/token",
                HttpMethod.POST,
                new HttpEntity<>(body, formHeaders()),
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody() == null ? Map.of() : response.getBody();
    }

    private MultiValueMap<String, String> tokenRequestBody(Map<String, String> fields) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        fields.forEach(body::add);
        return body;
    }

    private HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private TokenResponse toTokenResponse(Map<String, Object> tokenPayload, String tenantId, String appId) {
        String accessToken = stringValue(tokenPayload.get("access_token"));
        String refreshToken = stringValue(tokenPayload.get("refresh_token"));
        String tokenType = stringValue(tokenPayload.getOrDefault("token_type", "Bearer"));
        Long expiresIn = longValue(tokenPayload.getOrDefault("expires_in", 0L));

        Map<String, Object> responsePayload = new LinkedHashMap<>();
        responsePayload.put("tenantId", tenantId);
        responsePayload.put("tokenResponse", tokenPayload);

        return new TokenResponse(
                accessToken,
                refreshToken,
                tokenType,
                expiresIn,
                "KEYCLOAK",
                tenantId,
                appId,
                responsePayload
        );
    }

    private String keycloakBaseUrl() {
        String configured = authenticationProperties.getBaseUrl();
        return configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private Long longValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            return Long.valueOf(text);
        }
        return Long.valueOf(String.valueOf(value));
    }
}
