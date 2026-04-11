package com.avira.iamservice.realm.dto;

import com.avira.iamservice.realm.IdentityMode;

public record TenantRealmConfigResponse(String tenantId, IdentityMode identityMode, boolean dedicatedRealmApproved) {
}
