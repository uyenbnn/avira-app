package com.avira.iamservice.clientservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IamRealmProperties.class)
public class ClientServiceConfiguration {
}
