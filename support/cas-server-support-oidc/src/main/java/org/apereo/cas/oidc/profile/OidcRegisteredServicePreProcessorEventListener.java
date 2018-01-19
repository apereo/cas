package org.apereo.cas.oidc.profile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.springframework.context.event.EventListener;

/**
 * This is {@link OidcRegisteredServicePreProcessorEventListener}.
 * Tries to reconcile scopes into attribute release policies
 * for OIDC services when and as services are loaded.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class OidcRegisteredServicePreProcessorEventListener {

    private final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

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
