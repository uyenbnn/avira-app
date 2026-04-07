package com.avira.commonlib.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

public class KeycloakTokenWebClient {

    private static final Logger log = LoggerFactory.getLogger(KeycloakTokenWebClient.class);

    private final WebClient webClient;
    private final String tokenUrl;
    private final String logoutUrl;
    private final String clientId;
    private final String clientSecret;

    public KeycloakTokenWebClient(WebClient.Builder builder, String tokenUrl, String clientId, String clientSecret) {
        this(builder, tokenUrl, deriveLogoutUrl(tokenUrl), clientId, clientSecret);
    }

    public KeycloakTokenWebClient(WebClient.Builder builder,
                                  String tokenUrl,
                                  String logoutUrl,
                                  String clientId,
                                  String clientSecret) {
        this.webClient = builder.build();
        this.tokenUrl = tokenUrl;
        this.logoutUrl = logoutUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Map<String, Object> login(String email, String password) {
        MultiValueMap<String, String> form = baseForm();
        form.add("grant_type", "password");
        form.add("username", email);
        form.add("password", password);
        return exchange(form);
    }

    public Map<String, Object> refresh(String refreshToken) {
        MultiValueMap<String, String> form = baseForm();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        return exchange(form);
    }

    public void logout(String refreshToken) {
        MultiValueMap<String, String> form = baseForm();
        form.add("refresh_token", refreshToken);

        webClient.post()
                .uri(logoutUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private MultiValueMap<String, String> baseForm() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
        return form;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchange(MultiValueMap<String, String> form) {
        try {
            return webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(form)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .switchIfEmpty(Mono.error(new IllegalStateException("Keycloak token response is empty")))
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("Keycloak token request failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }

    private static String deriveLogoutUrl(String tokenUrl) {
        if (tokenUrl == null || tokenUrl.isBlank()) {
            throw new IllegalArgumentException("tokenUrl must not be blank");
        }
        if (tokenUrl.endsWith("/token")) {
            return tokenUrl.substring(0, tokenUrl.length() - "/token".length()) + "/logout";
        }
        return tokenUrl;
    }
}

