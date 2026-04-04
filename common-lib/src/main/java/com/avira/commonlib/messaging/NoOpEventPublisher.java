package com.avira.commonlib.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisher.class);

    @Override
    public void publish(EventEnvelope<?> event) {
        log.debug("Messaging disabled or no provider configured. Skipping event domain={} action={} eventId={}",
                event.domain(), event.action(), event.eventId());
    }
}

