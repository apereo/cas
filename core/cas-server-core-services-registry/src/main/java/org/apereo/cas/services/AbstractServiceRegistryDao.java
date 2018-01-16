package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This is {@link AbstractServiceRegistryDao}, that acts as the base parent class
 * for all registry implementations, capturing common ops.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class AbstractServiceRegistryDao implements ServiceRegistryDao {

    
    @Autowired
    private transient ApplicationEventPublisher eventPublisher;
    

    /**
     * Publish event.
     *
     * @param event the event
     */
    public void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            LOGGER.debug("Publishing event [{}]", event);
            this.eventPublisher.publishEvent(event);
        }
    }

    protected void setEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
