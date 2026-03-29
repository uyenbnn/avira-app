package com.avira.applicationinitializationservice.controller;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import com.avira.applicationinitializationservice.service.KeycloakInitializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
public class InitializationController {

    private final KeycloakInitializationService keycloakInitializationService;

    @PostMapping
    public ResponseEntity<InitializationResponse> initialize() {
        return ResponseEntity.ok(keycloakInitializationService.initialize());
    }
}

