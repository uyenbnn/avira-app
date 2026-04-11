package com.avira.iamservice.realm;

public record TenantRealmConfig(String tenantId, IdentityMode identityMode, boolean dedicatedRealmApproved) {
}
