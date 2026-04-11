package com.avira.iamservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.avira.iamservice.realm.config.IamRealmProperties;
import com.avira.iamservice.realm.config.KeycloakInitProperties;

@SpringBootApplication
@EnableConfigurationProperties({IamRealmProperties.class, KeycloakInitProperties.class})
public class IamServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IamServiceApplication.class, args);
	}

}
