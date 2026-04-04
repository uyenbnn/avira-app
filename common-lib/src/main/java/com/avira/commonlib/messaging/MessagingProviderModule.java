package com.avira.commonlib.messaging;

public interface MessagingProviderModule {

    MessagingProvider provider();

    TopicLifecycleManager createTopicLifecycleManager();

    EventPublisher createEventPublisher(TopicManager topicManager);
}

