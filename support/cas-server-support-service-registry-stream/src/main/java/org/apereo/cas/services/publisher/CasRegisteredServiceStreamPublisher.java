package org.apereo.cas.services.publisher;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.PublisherIdentifier;

import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CasRegisteredServiceStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface CasRegisteredServiceStreamPublisher {

    /**
     * Publish.
     *
     * @param service             the service
     * @param event               the event
     * @param publisherIdentifier the publisher identifier
     */
    void publish(RegisteredService service, ApplicationEvent event, PublisherIdentifier publisherIdentifier);
}
