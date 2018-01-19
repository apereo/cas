package org.apereo.cas.support.events.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

/**
 * This is {@link CasCloudBusConfigurationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class CasCloudBusConfigurationEventListener {
    private final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;
    private final ApplicationContext applicationContext;

    /**
     * Handle refresh event when issued by the cloud bus.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final RefreshRemoteApplicationEvent event) {
        LOGGER.debug("Received event [{}]", event);
        configurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.applicationContext);
    }
}
