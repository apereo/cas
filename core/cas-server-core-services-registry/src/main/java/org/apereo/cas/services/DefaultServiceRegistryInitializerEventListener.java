package org.apereo.cas.services;

import org.apereo.cas.config.CasConfigurationModifiedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;

/**
 * This is {@link DefaultServiceRegistryInitializerEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultServiceRegistryInitializerEventListener implements ServiceRegistryInitializerEventListener {
    private final ObjectProvider<ServiceRegistryInitializer> serviceRegistryInitializer;

    @Override
    public void handleRefreshScopeRefreshedEvent(final RefreshScopeRefreshedEvent event) {
        LOGGER.info("Refreshing application context beans eagerly...");
        rebind();
    }

    @Override
    public void handleEnvironmentChangeEvent(final EnvironmentChangeEvent event) {
        LOGGER.trace("Received event [{}]", event);
        rebind();
    }

    @Override
    public void handleConfigurationModifiedEvent(final CasConfigurationModifiedEvent event) {
        if (event.isEligibleForContextRefresh()) {
            rebind();
        }
    }
    
    private void rebind() {
        LOGGER.info("Refreshing CAS service registry configuration. Stand by...");
        serviceRegistryInitializer.getObject().initialize();
    }
}
