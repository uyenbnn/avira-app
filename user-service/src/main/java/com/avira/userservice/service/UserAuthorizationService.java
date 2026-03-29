package com.avira.userservice.service;

import com.avira.userservice.enums.AuthProvider;
import com.avira.userservice.repository.UserAuthProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAuthorization")
@RequiredArgsConstructor
public class UserAuthorizationService {

    private final UserAuthProviderRepository userAuthProviderRepository;

    public boolean canAccessUser(Authentication authentication, String userId) {
        if (authentication == null || !authentication.isAuthenticated() || userId == null || userId.isBlank()) {
            return false;
        }

        return userAuthProviderRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, authentication.getName())
                .map(link -> userId.equals(link.getUser().getId()))
                .orElse(false);
    }
}

