package org.apereo.cas.services;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.function.Consumer;

/**
 * This is {@link AbstractServiceRegistryDao}, that acts as the base parent class
 * for all registry implementations, capturing common ops.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class AbstractServiceRegistryDao implements ServiceRegistryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceRegistryDao.class);

    /** Event consumer. */
    protected final Consumer<AbstractCasEvent> casEventConsumer = this::publishEvent;

    @Autowired
    private transient ApplicationEventPublisher eventPublisher;

    @Override
    public RegisteredService findServiceByExactServiceId(final String id) {
        return load().stream().filter(r -> r.getServiceId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Publish event.
     *
     * @param event the event
     */
    protected void publishEvent(final ApplicationEvent event) {
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
