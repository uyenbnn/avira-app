package com.avira.applicationinitializationservice.service;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.messaging.TopicManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagingInitializationServiceTest {

	@Mock
	private TopicManager topicManager;

	@InjectMocks
	private MessagingInitializationService messagingInitializationService;

	@Test
	void shouldReportCreatedAndExistingStreams() {
		when(topicManager.createTopic(EventTopics.USER_DOMAIN)).thenReturn(true);
		when(topicManager.createTopic(EventTopics.APPLICATION_DOMAIN)).thenReturn(false);
		when(topicManager.resolveTopicName(EventTopics.USER_DOMAIN)).thenReturn("user-domain");
		when(topicManager.resolveTopicName(EventTopics.APPLICATION_DOMAIN)).thenReturn("application-domain");

		InitializationResponse.MessagingInitialization response = messagingInitializationService.initializeMessaging();

		assertThat(response.created()).containsExactly("user-domain");
		assertThat(response.existing()).containsExactly("application-domain");
	}
}

