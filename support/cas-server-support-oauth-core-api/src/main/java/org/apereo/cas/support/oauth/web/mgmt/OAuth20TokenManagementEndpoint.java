package org.apereo.cas.support.oauth.web.mgmt;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * This is {@link OAuth20TokenManagementEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "oauthTokens", enableByDefault = false)
@Slf4j
public class OAuth20TokenManagementEndpoint extends BaseCasActuatorEndpoint {
    /**
     * The Ticket registry.
     */
    private final TicketRegistry ticketRegistry;

    public OAuth20TokenManagementEndpoint(final CasConfigurationProperties casProperties,
                                          final TicketRegistry ticketRegistry) {
        super(casProperties);
        this.ticketRegistry = ticketRegistry;
    }

    /**
     * Gets access tokens.
     *
     * @return the access tokens
     */
    @ReadOperation
    public Collection<Ticket> getTokens() {
        return ticketRegistry.getTickets(ticket -> (ticket instanceof AccessToken || ticket instanceof RefreshToken) && !ticket.isExpired())
            .sorted(Comparator.comparing(Ticket::getId))
            .collect(Collectors.toList());
    }


    /**
     * Gets access token.
     *
     * @param ticketId the ticket id
     * @return the access token
     */
    @ReadOperation
    public Ticket getToken(@Selector final String ticketId) {
        var ticket = (Ticket) ticketRegistry.getTicket(ticketId, AccessToken.class);
        if (ticket == null) {
            ticket = ticketRegistry.getTicket(ticketId, RefreshToken.class);
        }
        if (ticket == null) {
            LOGGER.debug("Ticket [{}] is not found", ticketId);
            return null;
        }
        if (ticket.isExpired()) {
            LOGGER.debug("Ticket [{}] is has expired", ticketId);
            return null;
        }
        return ticket;
    }

    /**
     * Delete access token.
     *
     * @param ticketId the ticket id
     */
    @DeleteOperation
    public void deleteToken(@Selector final String ticketId) {
        val ticket = getToken(ticketId);
        if (ticket != null) {
            ticketRegistry.deleteTicket(ticket.getId());
        }
    }
}
