package org.apereo.cas.services;

import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;

/**
 * This is {@link CasServiceRegistryInitializerConfigurationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasServiceRegistryInitializerConfigurationEventListener {
    private final ServiceRegistryInitializer serviceRegistryInitializer;

    /**
     * Handle refresh event when issued to this CAS server locally.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final EnvironmentChangeEvent event) {
        LOGGER.debug("Received event [{}]", event);
        rebind();
    }

    /**
     * Handle configuration modified event.
     *
     * @param event the event
     */
    @EventListener
    public void handleConfigurationModifiedEvent(final CasConfigurationModifiedEvent event) {
        if (event.isEligibleForContextRefresh()) {
            rebind();
        }
    }

    private void rebind() {
        LOGGER.info("Refreshing CAS service registry configuration. Stand by...");
        serviceRegistryInitializer.initServiceRegistryIfNecessary();
    }
}
