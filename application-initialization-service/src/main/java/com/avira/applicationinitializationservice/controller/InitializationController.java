package com.avira.applicationinitializationservice.controller;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import com.avira.applicationinitializationservice.service.InitializationOrchestrationService;
import com.avira.applicationinitializationservice.service.KeycloakInitializationService;
import com.avira.applicationinitializationservice.service.MessagingInitializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
public class InitializationController {

    private final InitializationOrchestrationService initializationOrchestrationService;
    private final KeycloakInitializationService keycloakInitializationService;
    private final MessagingInitializationService messagingInitializationService;

    @PostMapping
    public ResponseEntity<InitializationResponse> initialize() {
        return ResponseEntity.ok(initializationOrchestrationService.initializeAll());
    }

    @PostMapping("/keycloak")
    public ResponseEntity<InitializationResponse> initializeKeycloak() {
        return ResponseEntity.ok(InitializationResponse.forKeycloak(keycloakInitializationService.initializeKeycloak()));
    }

    @PostMapping({"/messaging", "/streams"})
    public ResponseEntity<InitializationResponse> initializeMessaging() {
        return ResponseEntity.ok(InitializationResponse.forMessaging(messagingInitializationService.initializeMessaging()));
    }
}

