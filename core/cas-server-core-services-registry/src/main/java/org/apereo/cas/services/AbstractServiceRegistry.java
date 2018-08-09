package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.Setter;

/**
 * This is {@link AbstractServiceRegistry}, that acts as the base parent class
 * for all registry implementations, capturing common ops.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Setter
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    @Autowired
    private transient ApplicationEventPublisher eventPublisher;

    /**
     * Publish event.
     *
     * @param event the event
     */
    public void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            LOGGER.trace("Publishing event [{}]", event);
            this.eventPublisher.publishEvent(event);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
