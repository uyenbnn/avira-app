package com.avira.commonlib.messaging;

public interface TopicLifecycleManager {

	boolean createTopic(String topicName);

	boolean deleteTopic(String topicName);
}

