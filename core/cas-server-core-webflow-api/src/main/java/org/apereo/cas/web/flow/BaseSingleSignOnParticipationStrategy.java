package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * This is {@link BaseSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public abstract class BaseSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * The Ticket registry support.
     */
    protected final TicketRegistrySupport ticketRegistrySupport;

    /**
     * The Service selection strategy.
     */
    protected final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    /**
     * Gets authentication from.
     *
     * @param ssoRequest the sso request
     * @return the authentication from
     */
    protected static Authentication getAuthenticationFrom(final SingleSignOnParticipationRequest ssoRequest) {
        return ssoRequest.getRequestContext().map(WebUtils::getAuthentication)
            .orElse(ssoRequest.getAttributeValue(Authentication.class.getName(), Authentication.class));
    }

    /**
     * Gets ticket granting ticket id.
     *
     * @param ssoRequest the sso request
     * @return the ticket granting ticket id
     */
    protected static Optional<String> getTicketGrantingTicketId(final SingleSignOnParticipationRequest ssoRequest) {
        return Optional.ofNullable(ssoRequest.getRequestContext()
            .map(WebUtils::getTicketGrantingTicketId)
            .orElse(ssoRequest.getAttributeValue(TicketGrantingTicket.class.getName(), String.class)));
    }

    /**
     * Determine registered service.
     *
     * @param ssoRequest the sso request
     * @return the registered service
     */
    protected RegisteredService getRegisteredService(final SingleSignOnParticipationRequest ssoRequest) {
        return ssoRequest.getRequestContext()
            .map(requestContext -> WebUtils.resolveRegisteredService(requestContext, servicesManager, serviceSelectionStrategy))
            .orElse(ssoRequest.getAttributeValue(RegisteredService.class.getName(), RegisteredService.class));
    }

    /**
     * Gets ticket state.
     *
     * @param ssoRequest the sso request
     * @return the ticket state
     */
    protected Optional<TicketState> getTicketState(final SingleSignOnParticipationRequest ssoRequest) {
        val tgtId = getTicketGrantingTicketId(ssoRequest).orElse(StringUtils.EMPTY);
        return Optional.ofNullable(ticketRegistrySupport.getTicketState(tgtId));
    }
}
