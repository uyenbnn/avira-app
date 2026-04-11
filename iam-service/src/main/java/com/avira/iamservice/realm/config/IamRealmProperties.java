package com.avira.iamservice.realm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iam.realm")
public record IamRealmProperties(String sharedName, String dedicatedPrefix) {
}
