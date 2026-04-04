package com.avira.projectservice.messaging;

import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.UserDomainActions;
import com.avira.commonlib.messaging.EventConsumer;
import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.user.UserRegisteredEvent;
import com.avira.projectservice.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventConsumer implements EventConsumer<UserRegisteredEvent> {

    private final TenantService tenantService;

    @Override
    public String domain() {
        return EventTopics.USER_DOMAIN;
    }

    @Override
    public String action() {
        return UserDomainActions.REGISTERED;
    }

    @Override
    public Class<UserRegisteredEvent> payloadType() {
        return UserRegisteredEvent.class;
    }

    @Override
    public void handle(EventEnvelope<UserRegisteredEvent> event) {
        log.info("Handling user-domain registered event eventId={} userId={} username={}",
                event.eventId(), event.payload().userId(), event.payload().username());
        tenantService.createDefaultTenantForUser(event.payload());
    }
}

