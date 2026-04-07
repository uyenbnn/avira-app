package com.avira.iamservice.initservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iam.realm")
public class IamRealmProperties {

    private String sharedName = "avira-platform";

    public String getSharedName() {
        return sharedName;
    }

    public void setSharedName(String sharedName) {
        this.sharedName = sharedName;
    }
}


