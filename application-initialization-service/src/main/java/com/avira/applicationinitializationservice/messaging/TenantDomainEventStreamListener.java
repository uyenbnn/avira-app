package com.avira.applicationinitializationservice.messaging;

import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.TenantDomainActions;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(Environment.class)
@ConditionalOnProperty(prefix = "avira.messaging", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "avira.messaging", name = "provider", havingValue = "rabbitmq-stream")
public class TenantDomainEventStreamListener implements InitializingBean, DisposableBean {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final MessagingProperties messagingProperties;
    private final EventConsumerSupport eventConsumerSupport;
    private final TenantCreatedEventConsumer tenantCreatedEventConsumer;
    private final TenantAuthenticationEnabledEventConsumer tenantAuthenticationEnabledEventConsumer;

    @Value("${spring.application.name:application-initialization-service}")
    private String applicationName;

    private Consumer consumer;

    @Override
    public void afterPropertiesSet() {
        String streamName = messagingProperties.resolveTopicName(EventTopics.TENANT_DOMAIN);
        consumer = environment.consumerBuilder()
                .stream(streamName)
                .name(applicationName + "-tenant-domain-consumer")
                .offset(OffsetSpecification.first())
                .messageHandler((context, message) -> handleMessage(streamName, message))
                .build();
        log.info("Started RabbitMQ Stream consumer '{}' for stream '{}'", applicationName + "-tenant-domain-consumer", streamName);
    }

    private void handleMessage(String streamName, Message message) {
        try {
            EventEnvelope<?> rawEvent = objectMapper.readValue(message.getBodyAsBinary(), EventEnvelope.class);
            String action = rawEvent.action();
            if (TenantDomainActions.CREATED.equals(action)) {
                eventConsumerSupport.consume(rawEvent, tenantCreatedEventConsumer);
            } else if (TenantDomainActions.AUTHENTICATION_ENABLED.equals(action)) {
                eventConsumerSupport.consume(rawEvent, tenantAuthenticationEnabledEventConsumer);
            } else {
                log.warn("No consumer registered for action '{}' on tenant-domain stream", action);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to consume message from stream '" + streamName + "'", ex);
        }
    }

    @Override
    public void destroy() {
        if (consumer != null) {
            consumer.close();
            log.info("Stopped RabbitMQ Stream consumer for application '{}'", applicationName);
        }
    }
}

