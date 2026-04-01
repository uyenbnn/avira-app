package com.avira.commonlib.messaging;

public interface EventConsumer<T> {

    String domain();

    String action();

    Class<T> payloadType();

    void handle(EventEnvelope<T> event);

    default String consumerId() {
        return getClass().getSimpleName();
    }
}

