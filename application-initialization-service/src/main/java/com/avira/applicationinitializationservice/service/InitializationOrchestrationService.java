package com.avira.applicationinitializationservice.service;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitializationOrchestrationService implements ApplicationRunner {

    private final KeycloakInitializationService keycloakInitializationService;
    private final MessagingInitializationService messagingInitializationService;

    @Value("${application.init.auto-run:false}")
    private boolean autoRun;

    @Value("${application.init.fail-fast:true}")
    private boolean failFast;

    @Override
    public void run(ApplicationArguments args) {

        if (!autoRun) {
            return;
        }

        try {
            initializeAll();
        } catch (Exception ex) {
            if (failFast) {
                throw new IllegalStateException("Application initialization failed: " + ex.getMessage(), ex);
            }
            log.error("Application initialization failed", ex);
        }
    }

    public InitializationResponse initializeAll() {
        return InitializationResponse.forAll(
                keycloakInitializationService.initializeKeycloak(),
                messagingInitializationService.initializeMessaging()
        );
    }

}

