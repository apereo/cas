package org.apereo.cas.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;

/**
 * This is {@link AbstractServiceRegistry}, that acts as the base parent class
 * for all registry implementations, capturing common ops.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    /**
     * The Event publisher.
     */
    private final transient ConfigurableApplicationContext applicationContext;

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
        if (this.applicationContext != null) {
            LOGGER.trace("Publishing event [{}]", event);
            this.applicationContext.publishEvent(event);
        }
    }

    /**
     * Invoke service registry listener pre save.
     *
     * @param registeredService the registered service
     * @return the registered service
     */
    protected RegisteredService invokeServiceRegistryListenerPreSave(final RegisteredService registeredService) {
        if (serviceRegistryListeners != null) {
            serviceRegistryListeners.forEach(listener -> listener.preSave(registeredService));
        }
        return registeredService;
    }

    /**
     * Invoke service registry listener post load.
     *
     * @param registeredService the registered service
     * @return the registered service
     */
    protected RegisteredService invokeServiceRegistryListenerPostLoad(final RegisteredService registeredService) {
        if (serviceRegistryListeners != null) {
            serviceRegistryListeners.forEach(listener -> listener.postLoad(registeredService));
        }
        return registeredService;
    }
}
