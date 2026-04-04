package com.avira.applicationinitializationservice.service;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitializationOrchestrationServiceTest {

    @Mock
    private KeycloakInitializationService keycloakInitializationService;

    @Mock
    private MessagingInitializationService messagingInitializationService;

    @Mock
    private ApplicationArguments args;

    @Test
    void shouldInitializeKeycloakAndStreamsInOrderForManualCall() {
        InitializationOrchestrationService service = new InitializationOrchestrationService(
                keycloakInitializationService,
                messagingInitializationService
        );

        InitializationResponse.KeycloakInitialization keycloakResponse =
                new InitializationResponse.KeycloakInitialization("avira", true, true, true, true, true);
        InitializationResponse.MessagingInitialization messagingResponse =
                new InitializationResponse.MessagingInitialization(java.util.List.of("user-domain"), java.util.List.of());
        when(keycloakInitializationService.initializeKeycloak()).thenReturn(keycloakResponse);
        when(messagingInitializationService.initializeMessaging()).thenReturn(messagingResponse);

        InitializationResponse actual = service.initializeAll();

        assertThat(actual).isEqualTo(InitializationResponse.forAll(keycloakResponse, messagingResponse));
        verify(keycloakInitializationService).initializeKeycloak();
        verify(messagingInitializationService).initializeMessaging();
    }

    @Test
    void shouldThrowWhenAutoRunFailsAndFailFastEnabled() {
        InitializationOrchestrationService service = new InitializationOrchestrationService(
                keycloakInitializationService,
                messagingInitializationService
        );
        ReflectionTestUtils.setField(service, "autoRun", true);
        ReflectionTestUtils.setField(service, "failFast", true);

        when(keycloakInitializationService.initializeKeycloak()).thenThrow(new RuntimeException("boom"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.run(args));

        assertThat(ex.getMessage()).contains("Application initialization failed");
        verify(keycloakInitializationService).initializeKeycloak();
        verify(messagingInitializationService, never()).initializeMessaging();
    }
}

