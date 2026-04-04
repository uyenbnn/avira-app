package com.avira.authenticationservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

    private String name = "authentication-service";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

