package com.avira.commonlib.messaging.rabbit;

import com.avira.commonlib.messaging.TopicLifecycleManager;
import com.rabbitmq.stream.Environment;
import org.springframework.stereotype.Component;

public class RabbitStreamTopicLifecycleManager implements TopicLifecycleManager {

	private final Environment environment;

	public RabbitStreamTopicLifecycleManager(Environment environment) {
		this.environment = environment;
	}

	@Override
	public boolean createTopic(String topicName) {
		if (environment.streamExists(topicName)) {
			return false;
		}
		environment.streamCreator().stream(topicName).create();
		return true;
	}

	@Override
	public boolean deleteTopic(String topicName) {
		if (!environment.streamExists(topicName)) {
			return false;
		}
		environment.deleteStream(topicName);
		return true;
	}
}

