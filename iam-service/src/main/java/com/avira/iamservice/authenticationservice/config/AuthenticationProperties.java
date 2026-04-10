package com.avira.iamservice.authenticationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iam.auth.keycloak")
public class AuthenticationProperties {

    private String baseUrl = "http://localhost:8080";
    private String frontendClientId = "saas-frontend";
    private String backendClientId = "saas-backend";
    private String backendClientSecret = "change-me-backend-secret";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getFrontendClientId() {
        return frontendClientId;
    }

    public void setFrontendClientId(String frontendClientId) {
        this.frontendClientId = frontendClientId;
    }

    public String getBackendClientId() {
        return backendClientId;
    }

    public void setBackendClientId(String backendClientId) {
        this.backendClientId = backendClientId;
    }

    public String getBackendClientSecret() {
        return backendClientSecret;
    }

    public void setBackendClientSecret(String backendClientSecret) {
        this.backendClientSecret = backendClientSecret;
    }
}
