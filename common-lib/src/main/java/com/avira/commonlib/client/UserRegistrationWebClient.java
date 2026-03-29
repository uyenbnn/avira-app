package com.avira.commonlib.client;

import org.springframework.web.reactive.function.client.WebClient;

public class UserRegistrationWebClient {

    private final WebClient webClient;

    public UserRegistrationWebClient(WebClient.Builder builder, String userServiceBaseUrl) {
        this.webClient = builder.baseUrl(userServiceBaseUrl).build();
    }

    public <T> T register(Object requestBody, String uri, Class<T> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}

