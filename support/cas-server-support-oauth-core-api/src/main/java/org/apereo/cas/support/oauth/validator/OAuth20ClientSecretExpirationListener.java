package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.util.spring.CasEventListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * This is {@link OAuth20ClientSecretExpirationListener}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OAuth20ClientSecretExpirationListener extends CasEventListener {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oauth20ClientSecretExpirationListener";

    /**
     * Remove expired client secrets from the applications once they are loaded from the service registry.
     *
     * @param event the event
     */
    @EventListener
    @Async
    void handleRegisteredServicesLoadedEvent(CasRegisteredServicesLoadedEvent event);
}
