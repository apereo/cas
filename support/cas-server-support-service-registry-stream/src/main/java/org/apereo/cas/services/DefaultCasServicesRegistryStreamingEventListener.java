package org.apereo.cas.services;

import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.util.PublisherIdentifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * This is {@link DefaultCasServicesRegistryStreamingEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCasServicesRegistryStreamingEventListener implements CasServicesRegistryStreamingEventListener {
    private final CasRegisteredServiceStreamPublisher publisher;

    private final PublisherIdentifier publisherIdentifier;

    @Override
    @EventListener
    @Async
    public void handleCasRegisteredServiceLoadedEvent(final CasRegisteredServiceLoadedEvent event) {
        LOGGER.trace("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event, publisherIdentifier);
    }

    @Override
    @EventListener
    @Async
    public void handleCasRegisteredServiceSavedEvent(final CasRegisteredServiceSavedEvent event) {
        LOGGER.trace("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event, publisherIdentifier);
    }

    @Override
    @EventListener
    @Async
    public void handleCasRegisteredServiceDeletedEvent(final CasRegisteredServiceDeletedEvent event) {
        LOGGER.trace("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event, publisherIdentifier);
    }
}
