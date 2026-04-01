package com.avira.userservice.service;

import com.avira.commonlib.messaging.user.UserRegisteredEvent;
import com.avira.userservice.client.KeycloakAdminClient;
import com.avira.userservice.constants.RoleConstants;
import com.avira.userservice.dto.CreateUserRequest;
import com.avira.userservice.dto.UserResponse;
import com.avira.userservice.entity.User;
import com.avira.userservice.entity.UserAuthProvider;
import com.avira.userservice.entity.UserProfile;
import com.avira.userservice.enums.AuthProvider;
import com.avira.userservice.repository.UserAuthProviderRepository;
import com.avira.userservice.repository.UserProfileRepository;
import com.avira.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateDomainUserProfileAndKeycloakIdentity() {
        CreateUserRequest request = new CreateUserRequest(
                "alice@avira.com",
                "StrongPass123",
                "0123456789",
                "Alice",
                "Smith"
        );
        UserRepresentation identity = new UserRepresentation();
        identity.setId("kc-user-id");
        identity.setEmail("alice@avira.com");
        identity.setEmailVerified(false);

        when(keycloakAdminClient.createUser(eq("alice@avira.com"), eq("Alice"), eq("Smith"), eq("StrongPass123"), eq(false)))
                .thenReturn(Optional.of("kc-user-id"));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthProviderRepository.findFirstByUserIdAndProvider("kc-user-id", AuthProvider.LOCAL))
                .thenAnswer(invocation -> Optional.of(UserAuthProvider.builder()
                        .user(User.builder().id("kc-user-id").build())
                        .provider(AuthProvider.LOCAL)
                        .providerUserId("kc-user-id")
                        .email("alice@avira.com")
                        .build()));
        when(keycloakAdminClient.findById("kc-user-id")).thenReturn(Optional.of(identity));

        UserResponse result = userService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("kc-user-id");
        assertThat(result.email()).isEqualTo("alice@avira.com");
        assertThat(result.phone()).isEqualTo("0123456789");

        verify(keycloakAdminClient).createUser("alice@avira.com", "Alice", "Smith", "StrongPass123", false);
        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(userAuthProviderRepository).save(any(UserAuthProvider.class));
        verify(keycloakAdminClient).assignRole("kc-user-id", RoleConstants.USER);
        verify(keycloakAdminClient).findById("kc-user-id");
    }

    @Test
    void shouldCreateLocalUserFromRegistrationEvent() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "kc-user-id",
                "alice",
                "alice@avira.com",
                "Alice",
                "Smith"
        );

        when(userRepository.existsById("kc-user-id")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createFromRegisteredEvent(event);

        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(userAuthProviderRepository).save(any(UserAuthProvider.class));
    }

    @Test
    void shouldMirrorStatusChangesToLinkedIdentityProvider() {
        User user = User.builder()
                .id("domain-user-1")
                .build();
        UserAuthProvider authProvider = UserAuthProvider.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId("kc-user-id")
                .email("alice@avira.com")
                .build();

        when(userRepository.findById("domain-user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthProviderRepository.findFirstByUserIdAndProvider("domain-user-1", AuthProvider.LOCAL))
                .thenReturn(Optional.of(authProvider));

        userService.changeStatus("domain-user-1", com.avira.userservice.enums.UserStatus.DISABLED);

        verify(keycloakAdminClient).setUserEnabled("kc-user-id", false);
    }
}
