package org.apereo.cas.support.events.listener;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * This is {@link CasCloudBusConfigurationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasCloudBusConfigurationEventListener {
    private final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;
    private final ApplicationContext applicationContext;

    /**
     * Handle refresh event when issued by the cloud bus.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleRefreshEvent(final RefreshRemoteApplicationEvent event) {
        LOGGER.trace("Received event [{}]", event);
        configurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.applicationContext);
    }
}
