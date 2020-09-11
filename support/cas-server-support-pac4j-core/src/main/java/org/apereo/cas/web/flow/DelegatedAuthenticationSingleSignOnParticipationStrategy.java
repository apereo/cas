package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedAuthenticationSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedAuthenticationSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    private final TicketRegistrySupport ticketRegistrySupport;

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return true;
        }
        val accessStrategy = registeredService.getAccessStrategy();
        if (accessStrategy == null || accessStrategy.getDelegatedAuthenticationPolicy() == null) {
            return true;
        }

        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return true;
        }
        val ca = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        try {
            val authentication = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);
            val policy = accessStrategy.getDelegatedAuthenticationPolicy();
            val attributes = authentication.getAttributes();
            if (attributes.containsKey(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME)) {
                val clientNameAttr = attributes.get(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME);
                val value = CollectionUtils.firstElement(clientNameAttr);
                if (value.isPresent()) {
                    val client = value.get().toString();
                    LOGGER.debug("Evaluating delegated access strategy for client [{}] and service [{}]",
                        client, registeredService);
                    return policy.isProviderAllowed(client, registeredService);
                }
                return false;
            }
            return !policy.isProviderRequired();
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
    }

    @Override
    public boolean supports(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return false;
        }
        val accessStrategy = registeredService.getAccessStrategy();
        if (accessStrategy == null || accessStrategy.getDelegatedAuthenticationPolicy() == null) {
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private RegisteredService determineRegisteredService(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService != null) {
            return registeredService;
        }
        val service = WebUtils.getService(requestContext);
        val serviceToUse = serviceSelectionStrategy.resolveService(service);
        if (serviceToUse != null) {
            return this.servicesManager.findServiceBy(serviceToUse);
        }
        return null;
    }
}
