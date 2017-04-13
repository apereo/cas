package org.apereo.cas.support.events.listener;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.event.EventListener;

/**
 * This is {@link CasCloudBusConfigurationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasCloudBusConfigurationEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCloudBusConfigurationEventListener.class);
    
    private final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;

    public CasCloudBusConfigurationEventListener(final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager) {
        this.configurationPropertiesEnvironmentManager = configurationPropertiesEnvironmentManager;
    }

    /**
     * Handle refresh event when issued by the cloud bus.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final RefreshRemoteApplicationEvent event) {
        LOGGER.debug("Received event [{}]", event);
        configurationPropertiesEnvironmentManager.rebindCasConfigurationProperties();
    }
}
