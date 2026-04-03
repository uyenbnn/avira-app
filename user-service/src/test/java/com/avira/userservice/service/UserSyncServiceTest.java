package com.avira.userservice.service;

import com.avira.commonlib.messaging.user.UserRegisteredEvent;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
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

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateDomainOnlyUserProfileWithoutKeycloakIdentity() {
        CreateUserRequest request = new CreateUserRequest(
                "alice@avira.com",
                "0123456789",
                "Alice",
                "Smith"
        );

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null || user.getId().isBlank()) {
                user.setId(UUID.randomUUID().toString());
            }
            return user;
        });
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthProviderRepository.findFirstByUserIdAndProvider(argThat(id -> id != null && !id.isBlank()), eq(AuthProvider.LOCAL)))
                .thenAnswer(invocation -> {
                    String userId = invocation.getArgument(0);
                    return Optional.of(UserAuthProvider.builder()
                            .user(User.builder().id(userId).build())
                            .provider(AuthProvider.LOCAL)
                            .providerUserId(userId)
                            .email("alice@avira.com")
                            .build());
                });

        UserResponse result = userService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotBlank();
        assertThat(result.email()).isEqualTo("alice@avira.com");
        assertThat(result.phone()).isEqualTo("0123456789");
        assertThat(result.emailVerified()).isFalse();

        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(userAuthProviderRepository).save(any(UserAuthProvider.class));
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
    void shouldUpdateStatusInLocalStoreOnly() {
        User user = User.builder()
                .id("domain-user-1")
                .build();

        when(userRepository.findById("domain-user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changeStatus("domain-user-1", com.avira.userservice.enums.UserStatus.DISABLED);

        verify(userRepository).save(any(User.class));
    }
}
