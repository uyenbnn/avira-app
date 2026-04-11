package com.avira.iamservice.realm.dto;

import com.avira.iamservice.realm.IdentityMode;

public record TenantRealmConfigRequest(String tenantId, IdentityMode identityMode) {
}
