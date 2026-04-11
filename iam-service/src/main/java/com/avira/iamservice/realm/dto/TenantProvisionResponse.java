package com.avira.iamservice.realm.dto;

public record TenantProvisionResponse(String tenantId, String realm, String keycloakClientId, String status) {
}
