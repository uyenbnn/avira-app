package com.avira.commonlib.messaging;

public class NoOpTopicLifecycleManager implements TopicLifecycleManager {

	@Override
	public boolean createTopic(String topicName) {
		return false;
	}

	@Override
	public boolean deleteTopic(String topicName) {
		return false;
	}
}

