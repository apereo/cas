package org.apereo.cas.support.events.listener;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CasConfigurationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasConfigurationEventListener {

    private final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;

    private final ConfigurationPropertiesBindingPostProcessor binder;

    private final ContextRefresher contextRefresher;

    private final ApplicationContext applicationContext;

    /**
     * Handle refresh event when issued to this CAS server locally.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleRefreshEvent(final EnvironmentChangeEvent event) {
        LOGGER.trace("Received event [{}]", event);
        rebind();
    }

    /**
     * Handle configuration modified event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleConfigurationModifiedEvent(final CasConfigurationModifiedEvent event) {
        if (this.contextRefresher == null) {
            LOGGER.warn("Unable to refresh application context, since no refresher is available");
            return;
        }

        if (event.isEligibleForContextRefresh()) {
            LOGGER.info("Received event [{}]. Refreshing CAS configuration...", event);
            Collection<String> keys = null;
            try {
                keys = contextRefresher.refresh();
                LOGGER.debug("Refreshed the following settings: [{}].", keys);
            } catch (final Exception e) {
                LOGGER.trace(e.getMessage(), e);
            } finally {
                rebind();
                LOGGER.info("CAS finished rebinding configuration with new settings [{}]",
                    ObjectUtils.defaultIfNull(keys, new ArrayList<>(0)));
            }
        }
    }

    private void rebind() {
        LOGGER.info("Refreshing CAS configuration. Stand by...");
        if (configurationPropertiesEnvironmentManager != null) {
            configurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.applicationContext);
        } else {
            CasConfigurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.binder, this.applicationContext);
        }
    }
}
