package com.avira.projectservice.messaging;

import com.avira.commonlib.config.properties.ApplicationProperties;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.messaging.EventConsumerSupport;
import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.OffsetSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(Environment.class)
@ConditionalOnProperty(prefix = "avira.messaging", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "avira.messaging", name = "provider", havingValue = "rabbitmq-stream")
public class UserDomainEventStreamListener implements InitializingBean, DisposableBean {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final MessagingProperties messagingProperties;
    private final ApplicationProperties applicationProperties;
    private final EventConsumerSupport eventConsumerSupport;
    private final UserRegisteredEventConsumer userRegisteredEventConsumer;

    private Consumer consumer;

    @Override
    public void afterPropertiesSet() {
        String streamName = messagingProperties.resolveTopicName(EventTopics.USER_DOMAIN);
        String applicationName = applicationProperties.getName();
        consumer = environment.consumerBuilder()
                .stream(streamName)
                .name(applicationName + "-user-domain-consumer")
                .offset(OffsetSpecification.first())
                .messageHandler((context, message) -> handleMessage(streamName, message))
                .build();
        log.info("Started RabbitMQ Stream consumer '{}' for stream '{}'",
                applicationName + "-user-domain-consumer", streamName);
    }

    private void handleMessage(String streamName, Message message) {
        try {
            EventEnvelope<?> rawEvent = objectMapper.readValue(message.getBodyAsBinary(), EventEnvelope.class);
            eventConsumerSupport.consume(rawEvent, userRegisteredEventConsumer);
        } catch (Exception ex) {
            log.error("Failed to consume message from stream '{}': {}", streamName, ex.getMessage(), ex);
        }
    }

    @Override
    public void destroy() {
        if (consumer != null) {
            consumer.close();
            log.info("Stopped RabbitMQ Stream consumer for application '{}'", applicationProperties.getName());
        }
    }
}

