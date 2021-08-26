package org.apereo.cas.services.publisher;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.BaseCasRegisteredServiceEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.util.PublisherIdentifier;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link BaseCasRegisteredServiceStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseCasRegisteredServiceStreamPublisher implements CasRegisteredServiceStreamPublisher {

    @Override
    public final void publish(final RegisteredService service, final ApplicationEvent event,
                              final PublisherIdentifier publisherIdentifier) {
        if (!BaseCasRegisteredServiceEvent.class.isAssignableFrom(event.getClass())) {
            return;
        }
        LOGGER.debug("Publishing service definition [{}] for event [{}] with publisher [{}]",
            service.getName(), event.getClass().getSimpleName(), publisherIdentifier);
        publishInternal(service, event, publisherIdentifier);
    }

    /**
     * Publish internal.
     *
     * @param service     the service
     * @param event       the event
     * @param publisherId the publisher id
     */
    protected void publishInternal(final RegisteredService service, final ApplicationEvent event,
                                   final PublisherIdentifier publisherId) {
        if (event instanceof CasRegisteredServiceDeletedEvent) {
            handleCasRegisteredServiceDeletedEvent(service, event, publisherId);
            return;
        }
        if (event instanceof CasRegisteredServiceSavedEvent || event instanceof CasRegisteredServiceLoadedEvent) {
            handleCasRegisteredServiceUpdateEvents(service, event, publisherId);
            return;
        }
        LOGGER.warn("Unsupported event [{}] for service replication", event);
    }

    /**
     * Handle cas registered service deleted event.
     *
     * @param service     the service
     * @param event       the event
     * @param publisherId the publisher id
     */
    protected void handleCasRegisteredServiceDeletedEvent(final RegisteredService service, final ApplicationEvent event,
                                                          final PublisherIdentifier publisherId) {
    }

    /**
     * Handle cas registered service update events.
     *
     * @param service     the service
     * @param event       the event
     * @param publisherId the publisher id
     */
    protected void handleCasRegisteredServiceUpdateEvents(final RegisteredService service, final ApplicationEvent event,
                                                          final PublisherIdentifier publisherId) {
    }
}
