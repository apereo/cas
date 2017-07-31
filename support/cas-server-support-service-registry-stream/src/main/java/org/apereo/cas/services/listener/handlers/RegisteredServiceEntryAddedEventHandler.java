package org.apereo.cas.services.listener.handlers;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.publisher.RegisteredServicesQueuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link RegisteredServiceEntryAddedEventHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RegisteredServiceEntryAddedEventHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceEntryAddedEventHandler.class);

    private final RegisteredServicesQueuedEvent event;
    private final ServicesManager servicesManager;

    public RegisteredServiceEntryAddedEventHandler(final RegisteredServicesQueuedEvent event,
                                                   final ServicesManager servicesManager) {
        this.event = event;
        this.servicesManager = servicesManager;
    }

    @Override
    public void run() {
        LOGGER.debug("Saving registered service definition [{}] received from [{}]", event.getService().getServiceId(), event.getPublisher());
        servicesManager.save(event.getService(), false);
    }
}
