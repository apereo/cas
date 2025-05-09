package org.apereo.cas.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
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

    private final ConfigurableApplicationContext applicationContext;

    private final Collection<ServiceRegistryListener> serviceRegistryListeners;

    @Setter
    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * Publish event.
     *
     * @param event the event
     */
    public void publishEvent(final ApplicationEvent event) {
        if (applicationContext != null) {
            LOGGER.trace("Publishing event [{}]", event);
            applicationContext.publishEvent(event);
        }
    }

    protected RegisteredService invokeServiceRegistryListenerPreSave(final RegisteredService registeredService) {
        if (serviceRegistryListeners != null) {
            serviceRegistryListeners.forEach(listener -> listener.preSave(registeredService));
        }
        return registeredService;
    }

    protected RegisteredService invokeServiceRegistryListenerPostLoad(final RegisteredService registeredService) {
        if (serviceRegistryListeners != null) {
            serviceRegistryListeners.forEach(listener -> listener.postLoad(registeredService));
        }
        return registeredService;
    }
}
