package com.avira.commonlib.messaging;

public class DefaultTopicManager implements TopicManager {

	private final MessagingProperties properties;
	private final TopicLifecycleManager topicLifecycleManager;

	public DefaultTopicManager(MessagingProperties properties,
							   TopicLifecycleManager topicLifecycleManager) {
		this.properties = properties;
		this.topicLifecycleManager = topicLifecycleManager;
	}

	@Override
	public String resolveTopicName(String domain) {
		return properties.resolveTopicName(domain);
	}

	@Override
	public boolean createTopic(String domain) {
		return topicLifecycleManager.createTopic(resolveTopicName(domain));
	}

	@Override
	public boolean deleteTopic(String domain) {
		return topicLifecycleManager.deleteTopic(resolveTopicName(domain));
	}
}

