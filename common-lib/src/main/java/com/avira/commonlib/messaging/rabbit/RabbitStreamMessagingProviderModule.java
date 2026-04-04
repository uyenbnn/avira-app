package com.avira.commonlib.messaging.rabbit;

import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.MessagingProperties;
import com.avira.commonlib.messaging.MessagingProvider;
import com.avira.commonlib.messaging.MessagingProviderModule;
import com.avira.commonlib.messaging.TopicLifecycleManager;
import com.avira.commonlib.messaging.TopicManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Environment;

public class RabbitStreamMessagingProviderModule implements MessagingProviderModule {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final MessagingProperties properties;

    public RabbitStreamMessagingProviderModule(Environment environment,
                                               ObjectMapper objectMapper,
                                               MessagingProperties properties) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public MessagingProvider provider() {
        return MessagingProvider.RABBITMQ_STREAM;
    }

    @Override
    public TopicLifecycleManager createTopicLifecycleManager() {
        return new RabbitStreamTopicLifecycleManager(environment);
    }

    @Override
    public EventPublisher createEventPublisher(TopicManager topicManager) {
        return new RabbitStreamEventPublisher(environment, objectMapper, properties, topicManager);
    }
}

