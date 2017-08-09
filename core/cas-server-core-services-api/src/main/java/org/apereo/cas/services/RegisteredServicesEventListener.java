package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.springframework.context.event.EventListener;

/**
 * This is {@link RegisteredServicesEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServicesEventListener {
    private final ServicesManager servicesManager;

    public RegisteredServicesEventListener(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * Handle services manager refresh event.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final CasRegisteredServicesRefreshEvent event) {
        servicesManager.load();
    }

}
