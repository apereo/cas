package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
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
     * Handle registered service expired event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleRegisteredServiceExpiredEvent(CasRegisteredServiceExpiredEvent event);

    /**
     * Handle context refreshed event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleContextRefreshedEvent(ContextRefreshedEvent event);
}
