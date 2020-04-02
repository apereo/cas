package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RequiredAuthenticationHandlersSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class RequiredAuthenticationHandlersSingleSignOnParticipationStrategy
    implements SingleSignOnParticipationStrategy {

    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    private final TicketRegistrySupport ticketRegistrySupport;

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return true;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        if (authenticationPolicy == null || authenticationPolicy.getRequiredAuthenticationHandlers().isEmpty()) {
            return true;
        }

        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return true;
        }
        val authentication = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
        val ca = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        try {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);
            val handlers = authentication.getAttributes().get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS);
            if (!handlers.containsAll(authenticationPolicy.getRequiredAuthenticationHandlers())) {
                LOGGER.warn("Authentication context has not been established using required handlers [{}]",
                    authenticationPolicy.getRequiredAuthenticationHandlers());
                return false;
            }
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
        return true;
    }

    @Override
    public boolean supports(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return false;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        if (authenticationPolicy == null || authenticationPolicy.getRequiredAuthenticationHandlers().isEmpty()) {
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
