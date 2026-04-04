package com.avira.commonlib.autoconfigure;

import com.avira.commonlib.messaging.EventConsumer;
import com.avira.commonlib.messaging.EventConsumerSupport;
import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.MessagingProperties;
import com.avira.commonlib.messaging.NoOpEventPublisher;
import com.avira.commonlib.messaging.NoOpTopicLifecycleManager;
import com.avira.commonlib.messaging.TopicLifecycleManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    MessagingAutoConfiguration.class,
                    RabbitStreamMessagingAutoConfiguration.class
            ));

    @Test
    void shouldExposeNoOpPublisherWhenMessagingIsDisabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EventPublisher.class);
            assertThat(context.getBean(EventPublisher.class)).isInstanceOf(NoOpEventPublisher.class);

            MessagingProperties properties = context.getBean(MessagingProperties.class);
            assertThat(properties.isEnabled()).isFalse();
            assertThat(properties.resolveTopicName("user-domain")).isEqualTo("user-domain");
        });
    }

    @Test
    void shouldUseKafkaProviderModuleFallbackUntilKafkaIsImplemented() {
        contextRunner.withPropertyValues(
                "avira.messaging.enabled=true",
                "avira.messaging.provider=kafka"
        ).run(context -> {
            assertThat(context).hasSingleBean(EventPublisher.class);
            assertThat(context.getBean(EventPublisher.class)).isInstanceOf(NoOpEventPublisher.class);
            assertThat(context).hasSingleBean(TopicLifecycleManager.class);
            assertThat(context.getBean(TopicLifecycleManager.class)).isInstanceOf(NoOpTopicLifecycleManager.class);
        });
    }

    @Test
    void shouldConvertPayloadBeforeDelegatingToConsumer() {
        EventConsumerSupport support = new EventConsumerSupport(new com.fasterxml.jackson.databind.ObjectMapper());
        CapturingConsumer consumer = new CapturingConsumer();

        support.consume(EventEnvelope.of("user-domain", "registered", "authentication-service", "user-1", Map.of("name", "Alice")), consumer);

        assertThat(consumer.received.payload().name()).isEqualTo("Alice");
        assertThat(consumer.received.key()).isEqualTo("user-1");
        assertThat(consumer.received.action()).isEqualTo("registered");
    }

    private record UserCreatedPayload(String name) {
    }

    private static final class CapturingConsumer implements EventConsumer<UserCreatedPayload> {

        private EventEnvelope<UserCreatedPayload> received;

        @Override
        public String domain() {
            return "user-domain";
        }

        @Override
        public String action() {
            return "registered";
        }

        @Override
        public Class<UserCreatedPayload> payloadType() {
            return UserCreatedPayload.class;
        }

        @Override
        public void handle(EventEnvelope<UserCreatedPayload> event) {
            this.received = event;
        }
    }
}

