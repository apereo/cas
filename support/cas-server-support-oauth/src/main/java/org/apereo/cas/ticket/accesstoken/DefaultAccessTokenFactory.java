package org.apereo.cas.ticket.accesstoken;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import java.util.Collection;

/**
 * Default OAuth access token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultAccessTokenFactory implements AccessTokenFactory {

    /** Default instance for the ticket id generator. */
    protected final UniqueTicketIdGenerator accessTokenIdGenerator;

    /** ExpirationPolicy for refresh tokens. */
    protected final ExpirationPolicy expirationPolicy;

    public DefaultAccessTokenFactory(final ExpirationPolicy expirationPolicy) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy);
    }

    @Override
    public AccessToken create(final Service service, final Authentication authentication,
                              final TicketGrantingTicket ticketGrantingTicket, final Collection<String> scopes) {
        final String codeId = this.accessTokenIdGenerator.getNewTicketId(AccessToken.PREFIX);
        final AccessToken at = new AccessTokenImpl(codeId, service, authentication, 
                this.expirationPolicy, ticketGrantingTicket, scopes);
        if (ticketGrantingTicket != null) {
            ticketGrantingTicket.getDescendantTickets().add(at.getId());
        }
        return at;
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
