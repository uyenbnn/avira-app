package com.avira.applicationinitializationservice.controller;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import com.avira.applicationinitializationservice.service.InitializationOrchestrationService;
import com.avira.applicationinitializationservice.service.KeycloakInitializationService;
import com.avira.applicationinitializationservice.service.MessagingInitializationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitializationControllerTest {

	@Mock
	private InitializationOrchestrationService initializationOrchestrationService;

	@Mock
	private KeycloakInitializationService keycloakInitializationService;

	@Mock
	private MessagingInitializationService messagingInitializationService;

	@InjectMocks
	private InitializationController controller;

	@Test
	void shouldDelegateInitToOrchestrationService() {
		InitializationResponse expected = InitializationResponse.forAll(
				new InitializationResponse.KeycloakInitialization("avira", true, true, true, true, true),
				new InitializationResponse.MessagingInitialization(List.of("user-domain"), List.of())
		);
		when(initializationOrchestrationService.initializeAll()).thenReturn(expected);

		var response = controller.initialize();

		assertThat(response.getBody()).isEqualTo(expected);
		verify(initializationOrchestrationService).initializeAll();
	}

	@Test
	void shouldDelegateKeycloakInitToKeycloakService() {
		InitializationResponse.KeycloakInitialization keycloak =
				new InitializationResponse.KeycloakInitialization("avira", true, true, true, true, true);
		InitializationResponse expected = InitializationResponse.forKeycloak(keycloak);
		when(keycloakInitializationService.initializeKeycloak()).thenReturn(keycloak);

		var response = controller.initializeKeycloak();

		assertThat(response.getBody()).isEqualTo(expected);
		verify(keycloakInitializationService).initializeKeycloak();
	}

	@Test
	void shouldDelegateMessagingInitToMessagingService() {
		InitializationResponse.MessagingInitialization messaging =
				new InitializationResponse.MessagingInitialization(List.of("user-domain"), List.of());
		InitializationResponse expected = InitializationResponse.forMessaging(messaging);
		when(messagingInitializationService.initializeMessaging()).thenReturn(messaging);

		var response = controller.initializeMessaging();

		assertThat(response.getBody()).isEqualTo(expected);
		verify(messagingInitializationService).initializeMessaging();
	}
}

