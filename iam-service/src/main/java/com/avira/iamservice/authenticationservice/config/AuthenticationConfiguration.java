package com.avira.iamservice.authenticationservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
public class AuthenticationConfiguration {
}
