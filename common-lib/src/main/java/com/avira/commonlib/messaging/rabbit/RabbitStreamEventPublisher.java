package com.avira.commonlib.messaging.rabbit;

import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.MessagingProperties;
import com.avira.commonlib.messaging.TopicManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Environment;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RabbitStreamEventPublisher implements EventPublisher, DisposableBean {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final MessagingProperties properties;
    private final TopicManager topicManager;
    private final ConcurrentMap<String, RabbitStreamTemplate> templates = new ConcurrentHashMap<>();

    public RabbitStreamEventPublisher(Environment environment,
                                      ObjectMapper objectMapper,
                                      MessagingProperties properties,
                                      TopicManager topicManager) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.topicManager = topicManager;
    }

    @Override
    public void publish(EventEnvelope<?> event) {
        topicManager.createTopic(event.domain());
        String topicName = properties.resolveTopicName(event.domain());
        RabbitStreamTemplate template = templates.computeIfAbsent(topicName,
                name -> new RabbitStreamTemplate(environment, name));
        try {
            template.convertAndSend(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize event for domain '" + event.domain() + "'", ex);
        }
    }

    @Override
    public void destroy() {
        templates.values().forEach(RabbitStreamTemplate::close);
        templates.clear();
    }
}

