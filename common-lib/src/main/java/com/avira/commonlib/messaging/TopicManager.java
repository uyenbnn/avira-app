package com.avira.commonlib.messaging;

public interface TopicManager {

	String resolveTopicName(String domain);

	boolean createTopic(String domain);

	boolean deleteTopic(String domain);
}

