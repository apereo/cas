package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link DefaultSingleSignOnBuildingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSingleSignOnBuildingStrategy implements SingleSignOnBuildingStrategy {
    protected final TicketRegistrySupport ticketRegistrySupport;
    protected final CentralAuthenticationService centralAuthenticationService;
    protected final ConfigurableApplicationContext applicationContext;

    @Override
    public Ticket buildTicketGrantingTicket(final AuthenticationResult authenticationResult,
                                            final Authentication authentication,
                                            final String ticketGrantingTicket) {
        try {
            return shouldIssueTicketGrantingTicket(authentication, ticketGrantingTicket)
                ? createTicketGrantingTicket(authenticationResult, ticketGrantingTicket)
                : updateTicketGrantingTicket(authentication, ticketGrantingTicket);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            if (e instanceof final PrincipalException pe) {
                throw pe;
            }
            throw new InvalidTicketException(ticketGrantingTicket);
        }
    }

    protected Ticket createTicketGrantingTicket(final AuthenticationResult authenticationResult,
                                                final String ticketGrantingTicket) throws Throwable {
        if (StringUtils.isNotBlank(ticketGrantingTicket)) {
            removeTicketGrantingTicket(ticketGrantingTicket);

        }
        LOGGER.trace("Attempting to issue a new ticket-granting ticket...");
        return centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
    }

    protected void removeTicketGrantingTicket(final String ticketGrantingTicketId) throws Throwable {
        val ticket = (TicketGrantingTicket) ticketRegistrySupport.getTicket(ticketGrantingTicketId);

        if (ticket == null) {
            LOGGER.trace("Existing ticket-granting ticket [{}] not found or expired", ticketGrantingTicketId);
            return;
        }

        LOGGER.trace("Removing existing ticket-granting ticket [{}]", ticketGrantingTicketId);
        val clientInfo = ClientInfoHolder.getClientInfo();
        applicationContext.publishEvent(new CasRequestSingleLogoutEvent(this, ticket, clientInfo));

        ticketRegistrySupport.getTicketRegistry().deleteTicket(ticketGrantingTicketId);
        applicationContext.publishEvent(new CasTicketGrantingTicketDestroyedEvent(this, ticket, clientInfo));
    }

    protected Ticket updateTicketGrantingTicket(final Authentication authentication, final String ticketGrantingTicketId) throws Exception {
        LOGGER.debug("Updating existing ticket-granting ticket [{}]...", ticketGrantingTicketId);
        val grantingTicket = ticketRegistrySupport.getTicketRegistry().getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        updateAuthentication(grantingTicket, authentication);
        return ticketRegistrySupport.getTicketRegistry().updateTicket(grantingTicket);
    }

    protected static void updateAuthentication(final TicketGrantingTicket ticketGrantingTicket, final Authentication authentication) {
        ticketGrantingTicket.getAuthentication().updateAttributes(authentication);
    }

    protected boolean shouldIssueTicketGrantingTicket(final Authentication authentication,
                                                      final String ticketGrantingTicket) throws Throwable {
        LOGGER.trace("Located ticket-granting ticket in the context. Retrieving associated authentication");
        val authenticationFromTgt = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);

        if (authenticationFromTgt == null) {
            LOGGER.debug("Authentication session associated with [{}] is no longer valid", ticketGrantingTicket);
            if (StringUtils.isNotBlank(ticketGrantingTicket)) {
                removeTicketGrantingTicket(ticketGrantingTicket);
            }
            return true;
        }

        return isAuthenticationAttemptTheSame(authentication, authenticationFromTgt);
    }

    protected boolean isAuthenticationAttemptTheSame(final Authentication currentAuthentication,
                                                     final Authentication previousAuthentication) {
        if (currentAuthentication.isEqualTo(previousAuthentication)) {
            LOGGER.debug("Resulting authentication matches the authentication from context");
            return false;
        }
        LOGGER.debug("Resulting authentication is different from the context");
        return true;
    }
}
