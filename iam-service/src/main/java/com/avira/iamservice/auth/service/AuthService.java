package com.avira.iamservice.auth.service;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.avira.iamservice.auth.dto.LoginRequest;
import com.avira.iamservice.auth.dto.LogoutRequest;
import com.avira.iamservice.auth.dto.RefreshRequest;
import com.avira.iamservice.auth.dto.TokenResponse;

@Service
public class AuthService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 3600L;
    private static final String TOKEN_TYPE = "Bearer";

    private final Map<String, SessionEntry> refreshSessions = new ConcurrentHashMap<>();

    public TokenResponse login(LoginRequest request) {
        requireText(request.tenantId(), "tenantId is required");
        requireText(request.username(), "username is required");
        requireText(request.password(), "password is required");

        return issueTokens(request.tenantId(), request.username(), request.appId());
    }

    public TokenResponse refresh(RefreshRequest request) {
        requireText(request.tenantId(), "tenantId is required");
        requireText(request.refreshToken(), "refreshToken is required");

        SessionEntry entry = refreshSessions.remove(request.refreshToken());
        if (entry == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        if (!entry.tenantId().equals(request.tenantId())) {
            throw new IllegalArgumentException("refreshToken does not belong to tenant");
        }

        return issueTokens(entry.tenantId(), entry.username(), entry.appId());
    }

    public void logout(LogoutRequest request) {
        requireText(request.tenantId(), "tenantId is required");
        requireText(request.refreshToken(), "refreshToken is required");

        SessionEntry entry = refreshSessions.get(request.refreshToken());
        if (entry != null && entry.tenantId().equals(request.tenantId())) {
            refreshSessions.remove(request.refreshToken());
        }
    }

    private TokenResponse issueTokens(String tenantId, String username, String appId) {
        String refreshToken = UUID.randomUUID().toString();
        String accessTokenPayload = tenantId + ":" + username + ":" + nullToEmpty(appId) + ":" + Instant.now().getEpochSecond();
        String accessToken = Base64.getUrlEncoder().withoutPadding().encodeToString(accessTokenPayload.getBytes());

        refreshSessions.put(refreshToken, new SessionEntry(tenantId, username, appId));

        return new TokenResponse(accessToken, refreshToken, ACCESS_TOKEN_TTL_SECONDS, TOKEN_TYPE);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private record SessionEntry(String tenantId, String username, String appId) {
    }
}
