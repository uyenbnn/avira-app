package com.avira.iamservice.clientservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iam.realm")
public class IamRealmProperties {

    private String sharedName = "avira-platform";
    private String dedicatedPrefix = "tenant_";

    public String getSharedName() {
        return sharedName;
    }

    public void setSharedName(String sharedName) {
        this.sharedName = sharedName;
    }

    public String getDedicatedPrefix() {
        return dedicatedPrefix;
    }

    public void setDedicatedPrefix(String dedicatedPrefix) {
        this.dedicatedPrefix = dedicatedPrefix;
    }
}
