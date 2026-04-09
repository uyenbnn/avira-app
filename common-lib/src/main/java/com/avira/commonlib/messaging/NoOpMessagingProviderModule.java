package com.avira.commonlib.messaging;

public class NoOpMessagingProviderModule implements MessagingProviderModule {

    @Override
    public MessagingProvider provider() {
        return MessagingProvider.NONE;
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

