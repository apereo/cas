package org.apereo.cas.services.publisher;

import org.apereo.cas.StringBean;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.BaseCasRegisteredServiceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * This is {@link BaseCasRegisteredServiceStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseCasRegisteredServiceStreamPublisher implements CasRegisteredServiceStreamPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCasRegisteredServiceStreamPublisher.class);

    /**
     * Publisher id.
     */
    protected final StringBean publisherId;

    public BaseCasRegisteredServiceStreamPublisher(final StringBean publisherId) {
        this.publisherId = publisherId;
    }

    @Override
    public final void publish(final RegisteredService service, final ApplicationEvent event) {
        if (!BaseCasRegisteredServiceEvent.class.isAssignableFrom(event.getClass())) {
            return;
        }
        LOGGER.debug("Publishing service definition [{}] for event [{}] with publisher [{}]",
                service.getName(), event.getClass().getSimpleName(), this.publisherId);
        publishInternal(service, event);
    }

    /**
     * Publish internal.
     *
     * @param service the service
     * @param event   the event
     */
    protected abstract void publishInternal(RegisteredService service, ApplicationEvent event);

    /**
     * Gets event to publish.
     *
     * @param service the service
     * @param event   the event
     * @return the event to publish
     */
    protected RegisteredServicesQueuedEvent getEventToPublish(final RegisteredService service, final ApplicationEvent event) {
        return new RegisteredServicesQueuedEvent(LocalDateTime.now().toString(), event, service, this.publisherId);
    }
}
