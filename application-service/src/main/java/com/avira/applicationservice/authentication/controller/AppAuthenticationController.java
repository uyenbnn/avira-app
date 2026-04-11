package com.avira.applicationservice.authentication.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avira.applicationservice.authentication.dto.AppTokenResponse;
import com.avira.applicationservice.authentication.dto.TokenExchangeRequest;
import com.avira.applicationservice.authentication.service.TokenExchangeService;

@RestController
@RequestMapping("/api/apps")
public class AppAuthenticationController {
    private final TokenExchangeService tokenExchangeService;

    public AppAuthenticationController(TokenExchangeService tokenExchangeService) {
        this.tokenExchangeService = tokenExchangeService;
    }

    @PostMapping("/{appId}/auth/token-exchange")
    public AppTokenResponse tokenExchange(
            @PathVariable String appId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdFromToken,
            @RequestBody TokenExchangeRequest request
    ) {
        if (tenantIdFromToken == null || tenantIdFromToken.isBlank()) {
            throw new SecurityException("Missing tenant context from validated token");
        }
        return tokenExchangeService.exchange(tenantIdFromToken, appId, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
