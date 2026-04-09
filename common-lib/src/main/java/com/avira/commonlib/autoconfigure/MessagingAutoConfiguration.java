package com.avira.commonlib.autoconfigure;

import com.avira.commonlib.messaging.DefaultTopicManager;
import com.avira.commonlib.messaging.EventConsumerSupport;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.MessagingProperties;
import com.avira.commonlib.messaging.NoOpEventPublisher;
import com.avira.commonlib.messaging.NoOpTopicLifecycleManager;
import com.avira.commonlib.messaging.TopicLifecycleManager;
import com.avira.commonlib.messaging.TopicManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
public class MessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicLifecycleManager topicLifecycleManager() {
        return new NoOpTopicLifecycleManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public TopicManager topicManager(MessagingProperties properties,
                                     TopicLifecycleManager topicLifecycleManager) {
        return new DefaultTopicManager(properties, topicLifecycleManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventPublisher eventPublisher() {
        return new NoOpEventPublisher();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventConsumerSupport eventConsumerSupport(ObjectMapper objectMapper) {
        return new EventConsumerSupport(objectMapper);
    }
}
