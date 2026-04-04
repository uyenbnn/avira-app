package com.avira.commonlib.messaging;

import java.util.Map;

public interface EventPublisher {

    void publish(EventEnvelope<?> event);

    default void publish(String domain, String action, String source, Object payload) {
        publish(EventEnvelope.of(domain, action, source, payload));
    }

    default void publish(String domain, String action, String source, String key, Object payload) {
        publish(EventEnvelope.of(domain, action, source, key, payload));
    }

    default void publish(String domain,
                         String action,
                         String source,
                         String key,
                         String transactionId,
                         Object payload,
                         Map<String, String> headers) {
        publish(EventEnvelope.of(domain, action, source, key, transactionId, payload, headers));
    }
}

