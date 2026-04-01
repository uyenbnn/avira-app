package com.avira.applicationinitializationservice.service;

import com.avira.applicationinitializationservice.dto.InitializationResponse;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.messaging.TopicManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingInitializationService {
	@Value("${avira.messaging.enabled:false}")
	private boolean messagingEnabled;

	@Value("${avira.messaging.provider:none}")
	private String messagingProvider;

	@Value("${avira.messaging.topic-prefix:}")
	private String messagingTopicPrefix;

	@Value("${avira.messaging.rabbit-stream.host:localhost}")
	private String rabbitStreamHost;

	@Value("${avira.messaging.rabbit-stream.port:5552}")
	private int rabbitStreamPort;

	@Value("${avira.messaging.rabbit-stream.username:guest}")
	private String rabbitStreamUsername;

	@Value("${avira.messaging.rabbit-stream.virtual-host:/}")
	private String rabbitStreamVirtualHost;

	private static final List<String> MANAGED_DOMAINS = List.of(
			EventTopics.USER_DOMAIN,
			EventTopics.APPLICATION_DOMAIN
	);

	private final TopicManager topicManager;

	public InitializationResponse.MessagingInitialization initializeMessaging() {
		logMessagingConfiguration();
		List<String> created = new ArrayList<>();
		List<String> existing = new ArrayList<>();

		for (String domain : MANAGED_DOMAINS) {
			boolean wasCreated = topicManager.createTopic(domain);
			String topicName = topicManager.resolveTopicName(domain);
			if (wasCreated) {
				created.add(topicName);
				log.info("Created stream '{}' for domain '{}'", topicName, domain);
			} else {
				existing.add(topicName);
				log.info("Stream '{}' for domain '{}' already exists or messaging is disabled", topicName, domain);
			}
		}

		return new InitializationResponse.MessagingInitialization(created, existing);
	}

	private void logMessagingConfiguration() {
		String topicPrefixValue = (messagingTopicPrefix == null || messagingTopicPrefix.isBlank())
				? "<empty>"
				: messagingTopicPrefix;
		log.info("Messaging config: enabled={}, provider={}, topicPrefix='{}', rabbitStream={}:{} user='{}' vhost='{}'",
				messagingEnabled,
				messagingProvider,
				topicPrefixValue,
				rabbitStreamHost,
				rabbitStreamPort,
				rabbitStreamUsername,
				rabbitStreamVirtualHost);
	}
}

