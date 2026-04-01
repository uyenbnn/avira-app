package com.avira.commonlib.autoconfigure;

import com.avira.commonlib.messaging.DefaultTopicManager;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.MessagingProperties;
import com.avira.commonlib.messaging.TopicLifecycleManager;
import com.avira.commonlib.messaging.TopicManager;
import com.avira.commonlib.messaging.rabbit.RabbitStreamEventPublisher;
import com.avira.commonlib.messaging.rabbit.RabbitStreamTopicLifecycleManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Address;
import com.rabbitmq.stream.Environment;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;

@AutoConfiguration(before = MessagingAutoConfiguration.class)
@ConditionalOnClass({Environment.class, RabbitStreamTemplate.class})
@ConditionalOnProperty(prefix = "avira.messaging", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "avira.messaging", name = "provider", havingValue = "rabbitmq-stream")
public class RabbitStreamMessagingAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public Environment rabbitStreamEnvironment(MessagingProperties properties) {
        MessagingProperties.RabbitStream rabbit = properties.getRabbitStream();
        return Environment.builder()
                .host(rabbit.getHost())
                .port(rabbit.getPort())
                .username(rabbit.getUsername())
                .password(rabbit.getPassword())
                .virtualHost(rabbit.getVirtualHost())
                // Keep broker metadata port but force host to configured endpoint (e.g., localhost/LB)
                // so Docker-internal hostnames do not break local/service-to-broker connectivity.
                .addressResolver(address -> new Address(
                        rabbit.getHost(),
                        address == null ? rabbit.getPort() : address.port()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(TopicLifecycleManager.class)
    public TopicLifecycleManager rabbitStreamTopicLifecycleManager(Environment environment) {
        return new RabbitStreamTopicLifecycleManager(environment);
    }

    @Bean
    @ConditionalOnMissingBean(TopicManager.class)
    public TopicManager topicManager(MessagingProperties properties,
                                     TopicLifecycleManager topicLifecycleManager) {
        return new DefaultTopicManager(properties, topicLifecycleManager);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher rabbitStreamEventPublisher(Environment environment,
                                                     ObjectMapper objectMapper,
                                                     MessagingProperties properties,
                                                     TopicManager topicManager) {
        return new RabbitStreamEventPublisher(
                environment,
                objectMapper,
                properties,
                topicManager);
    }
}
