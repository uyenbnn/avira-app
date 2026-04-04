package com.avira.commonlib.messaging;

public class KafkaMessagingProviderModule implements MessagingProviderModule {

    @Override
    public MessagingProvider provider() {
        return MessagingProvider.KAFKA;
    }

    @Override
    public TopicLifecycleManager createTopicLifecycleManager() {
        return new NoOpTopicLifecycleManager();
    }

    @Override
    public EventPublisher createEventPublisher(TopicManager topicManager) {
        return new NoOpEventPublisher();
    }
}

