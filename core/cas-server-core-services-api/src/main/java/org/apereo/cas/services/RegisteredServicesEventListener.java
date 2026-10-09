package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.apereo.cas.util.spring.CasEventListener;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Interface for {@code DefaultRegisteredServicesEventListener} to allow spring {@code @Async} support to use JDK proxy.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
public interface RegisteredServicesEventListener extends CasEventListener {

    /**
     * Handle services manager refresh event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleRefreshEvent(CasRegisteredServicesRefreshEvent event);

    /**
     * Handle environment change event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleEnvironmentChangeEvent(EnvironmentChangeEvent event);

    /**
     * Handle context refreshed event. This is the single general lifecycle trigger for the
     * initial load of the services registry; the JSON-import initializer and the refresh
     * events above own their own reloads, and no second load is scheduled at application-ready
     * time. The load is asynchronous and therefore not a readiness guarantee: it blocks
     * neither the context refresh nor the point at which the server accepts requests.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleContextRefreshedEvent(ContextRefreshedEvent event);
    
    /**
     * Handle registered service expired event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleRegisteredServiceExpiredEvent(CasRegisteredServiceExpiredEvent event);

    /**
     * Cache a service that was saved outside the services manager,
     * such as a service definition file picked up by a registry watcher.
     *
     * @param event the event
     */
    @EventListener
    void handleRegisteredServiceSavedEvent(CasRegisteredServiceSavedEvent event);

    /**
     * Evict a service that was deleted outside the services manager.
     *
     * @param event the event
     */
    @EventListener
    void handleRegisteredServiceDeletedEvent(CasRegisteredServiceDeletedEvent event);

    /**
     * Reload the services manager when a registry reloaded itself outside the services manager.
     *
     * @param event the event
     */
    @EventListener
    void handleRegisteredServicesLoadedEvent(CasRegisteredServicesLoadedEvent event);

}
