package org.apereo.cas.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;

/**
 * This is {@link AbstractServiceRegistry}, that acts as the base parent class
 * for all registry implementations, capturing common ops.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    /**
     * The Event publisher.
     */
    private final transient ApplicationEventPublisher eventPublisher;
    /**
     * The Service registry listeners.
     */
    private final transient Collection<ServiceRegistryListener> serviceRegistryListeners;

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

    /**
     * Invoke service registry listener pre save.
     *
     * @param registeredService the registered service
     * @return the registered service
     */
    protected RegisteredService invokeServiceRegistryListenerPreSave(final RegisteredService registeredService) {
        serviceRegistryListeners.forEach(listener -> listener.preSave(registeredService));
        return registeredService;
    }

    /**
     * Invoke service registry listener post load.
     *
     * @param registeredService the registered service
     * @return the registered service
     */
    protected RegisteredService invokeServiceRegistryListenerPostLoad(final RegisteredService registeredService) {
        serviceRegistryListeners.forEach(listener -> listener.postLoad(registeredService));
        return registeredService;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
