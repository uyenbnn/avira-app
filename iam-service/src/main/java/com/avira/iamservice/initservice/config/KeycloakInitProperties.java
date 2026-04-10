package com.avira.iamservice.initservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iam.init.keycloak")
public class KeycloakInitProperties {

    private boolean enabled = true;
    private String baseUrl = "http://localhost:8080";
    private String adminRealm = "master";
    private String adminClientId = "admin-cli";
    private String adminUsername = "admin";
    private String adminPassword = "admin";
    private String sharedRealm = "avira-platform";
    private String defaultTenantId = "default-saas";
    private String dedicatedPrefix = "tenant_";
    private String frontendClientId = "saas-frontend";
    private String backendClientId = "saas-backend";
    private String backendClientSecret = "change-me-backend-secret";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAdminRealm() {
        return adminRealm;
    }

    public void setAdminRealm(String adminRealm) {
        this.adminRealm = adminRealm;
    }

    public String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getSharedRealm() {
        return sharedRealm;
    }

    public void setSharedRealm(String sharedRealm) {
        this.sharedRealm = sharedRealm;
    }

    public String getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(String defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    public String getDedicatedPrefix() {
        return dedicatedPrefix;
    }

    public void setDedicatedPrefix(String dedicatedPrefix) {
        this.dedicatedPrefix = dedicatedPrefix;
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
