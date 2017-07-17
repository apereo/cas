package org.apereo.cas.services;

import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * This is {@link CasServicesRegistryStreamingEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasServicesRegistryStreamingEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasServicesRegistryStreamingEventListener.class);

    private final CasRegisteredServiceStreamPublisher publisher;

    public CasServicesRegistryStreamingEventListener(final CasRegisteredServiceStreamPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Handle cas registered service loaded event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRegisteredServiceLoadedEvent(final CasRegisteredServiceLoadedEvent event) {
        LOGGER.debug("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event);
    }

    /**
     * Handle cas registered service saved event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRegisteredServiceSavedEvent(final CasRegisteredServiceSavedEvent event) {
        LOGGER.debug("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event);
    }

    /**
     * Handle cas registered service deleted event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRegisteredServiceDeletedEvent(final CasRegisteredServiceDeletedEvent event) {
        LOGGER.debug("Received event [{}]", event);
        this.publisher.publish(event.getRegisteredService(), event);
    }
}
