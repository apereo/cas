package org.apereo.cas.oidc.profile;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * This is {@link OidcRegisteredServicePreProcessorEventListener}.
 * Tries to reconcile scopes into attribute release policies
 * for OIDC services when and as services are loaded.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcRegisteredServicePreProcessorEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcRegisteredServicePreProcessorEventListener.class);

    private final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

    public OidcRegisteredServicePreProcessorEventListener(final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter) {
        this.scopeToAttributesFilter = scopeToAttributesFilter;
    }

    /**
     * Handle registered service loaded event.
     *
     * @param event the event
     */
    @EventListener
    public void handleRegisteredServicesLoadedEvent(final CasRegisteredServicesLoadedEvent event) {
        event.getServices()
                .stream()
                .filter(OidcRegisteredService.class::isInstance)
                .forEach(s -> {
                    LOGGER.debug("Attempting to reconcile scopes and attributes for service [{}] of type [{}]",
                            s.getServiceId(), s.getClass().getSimpleName());
                    this.scopeToAttributesFilter.reconcile(s);
                });
    }
}
