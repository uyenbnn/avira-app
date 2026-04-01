package com.avira.commonlib.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventConsumerSupport {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerSupport.class);

    private final ObjectMapper objectMapper;

    public EventConsumerSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> void consume(EventEnvelope<?> rawEvent, EventConsumer<T> consumer) {
        if (!consumer.domain().equals(rawEvent.domain()) || !consumer.action().equals(rawEvent.action())) {
            throw new IllegalArgumentException(
                    "Consumer '" + consumer.consumerId() + "' cannot handle event domain='"
                            + rawEvent.domain() + "', action='" + rawEvent.action() + "'");
        }

        T payload = objectMapper.convertValue(rawEvent.payload(), consumer.payloadType());
        EventEnvelope<T> typedEvent = rawEvent.withPayload(payload);

        log.debug("Consuming event domain={} action={} eventId={} consumer={}",
                typedEvent.domain(), typedEvent.action(), typedEvent.eventId(), consumer.consumerId());
        consumer.handle(typedEvent);
    }
}

