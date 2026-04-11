package com.avira.iamservice.realm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "iam.realm")
public record IamRealmProperties(
		@DefaultValue("avira-platform") String sharedName,
		@DefaultValue("tenant_") String dedicatedPrefix,
		@DefaultValue("saas") String saasRealmName,
		@DefaultValue("master") String adminRealm,
		@DefaultValue("saas-backend") String saasClientId,
		@DefaultValue("tenant-%s-backend") String tenantBackendClientPattern
) {
	public String tenantBackendClientId(String tenantId) {
		if (tenantBackendClientPattern.contains("%s")) {
			return tenantBackendClientPattern.formatted(tenantId);
		}
		return tenantBackendClientPattern + tenantId;
	}
}
