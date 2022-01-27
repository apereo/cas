package org.apereo.cas.support.events.listener;

import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.util.spring.CasEventListener;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Interface for {@code DefaultCasConfigurationEventListener} to allow spring {@code @Async} support to use JDK proxy.
 * @author Hal Deadman
 * @since 6.5.0
 */
public interface CasConfigurationEventListener extends CasEventListener {

    /**
     * Handle refresh event when issued to this CAS server locally.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleRefreshEvent(EnvironmentChangeEvent event);

    /**
     * Handle configuration modified event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleConfigurationModifiedEvent(CasConfigurationModifiedEvent event);
}
