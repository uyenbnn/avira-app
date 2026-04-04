package com.avira.userservice.service;

import com.avira.userservice.user.entity.User;
import com.avira.userservice.user.entity.UserAuthProvider;
import com.avira.userservice.user.enums.AuthProvider;
import com.avira.userservice.user.repository.UserAuthProviderRepository;
import com.avira.userservice.user.service.UserAuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthorizationServiceTest {

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @InjectMocks
    private UserAuthorizationService userAuthorizationService;

    @Test
    void shouldAllowAccessWhenAuthenticatedSubjectMatchesLinkedProviderUserId() {
        User user = User.builder().id("domain-user-1").build();
        UserAuthProvider link = UserAuthProvider.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId("kc-user-id")
                .build();
        when(userAuthProviderRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, "kc-user-id"))
                .thenReturn(Optional.of(link));

        boolean result = userAuthorizationService.canAccessUser(
                new TestingAuthenticationToken("kc-user-id", "n/a", "ROLE_USER"),
                "domain-user-1"
        );

        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyAccessWhenSubjectIsLinkedToAnotherDomainUser() {
        User user = User.builder().id("domain-user-2").build();
        UserAuthProvider link = UserAuthProvider.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId("kc-user-id")
                .build();
        when(userAuthProviderRepository.findByProviderAndProviderUserId(AuthProvider.LOCAL, "kc-user-id"))
                .thenReturn(Optional.of(link));

        boolean result = userAuthorizationService.canAccessUser(
                new TestingAuthenticationToken("kc-user-id", "n/a", "ROLE_USER"),
                "domain-user-1"
        );

        assertThat(result).isFalse();
    }
}
