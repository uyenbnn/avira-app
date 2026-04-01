package com.avira.commonlib.messaging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventEnvelope<T>(
        String eventId,
        String transactionId,
        String domain,
        String action,
        String source,
        String key,
        T payload,
        Instant occurredAt,
        Map<String, String> headers
) {

    public EventEnvelope {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        eventId = eventId == null || eventId.isBlank() ? UUID.randomUUID().toString() : eventId;
    }

    public static <T> EventEnvelope<T> of(String domain,
                                          String action,
                                          String destination,
                                          T payload) {
        return new EventEnvelope<>(null, null, domain, action, destination, null, payload, null, Map.of());
    }

    public static <T> EventEnvelope<T> of(String domain,
                                          String action,
                                          String destination,
                                          String key,
                                          T payload) {
        return new EventEnvelope<>(null, null, domain, action, destination, key, payload, null, Map.of());
    }

    public static <T> EventEnvelope<T> of(String domain,
                                          String action,
                                          String destination,
                                          String key,
                                          String transactionId,
                                          T payload,
                                          Map<String, String> headers) {
        return new EventEnvelope<>(null, transactionId, domain, action, destination, key, payload, null, headers);
    }

    public <U> EventEnvelope<U> withPayload(U newPayload) {
        return new EventEnvelope<>(eventId, transactionId, domain, action, source, key, newPayload, occurredAt, headers);
    }
}

