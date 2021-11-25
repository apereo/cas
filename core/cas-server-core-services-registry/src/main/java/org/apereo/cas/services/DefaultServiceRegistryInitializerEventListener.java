package org.apereo.cas.services;

import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * This is {@link DefaultServiceRegistryInitializerEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultServiceRegistryInitializerEventListener implements ServiceRegistryInitializerEventListener {
    private final ServiceRegistryInitializer serviceRegistryInitializer;

    @Override
    @EventListener
    @Async
    public void handleRefreshEvent(final EnvironmentChangeEvent event) {
        LOGGER.trace("Received event [{}]", event);
        rebind();
    }

    @Override
    @EventListener
    @Async
    public void handleConfigurationModifiedEvent(final CasConfigurationModifiedEvent event) {
        if (event.isEligibleForContextRefresh()) {
            rebind();
        }
    }

    private void rebind() {
        LOGGER.info("Refreshing CAS service registry configuration. Stand by...");
        serviceRegistryInitializer.initialize();
    }
}
