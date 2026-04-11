package com.avira.iamservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.avira.iamservice.realm.config.IamRealmProperties;

@SpringBootApplication
@EnableConfigurationProperties(IamRealmProperties.class)
public class IamServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IamServiceApplication.class, args);
	}

}
