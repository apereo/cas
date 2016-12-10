package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link TicketGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultTicketGrantingTicketFactory implements TicketGrantingTicketFactory {

    /**
     * UniqueTicketIdGenerator to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    protected UniqueTicketIdGenerator tgtIdGenerator;

    /**
     * Expiration policy for ticket granting tickets.
     */
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    public DefaultTicketGrantingTicketFactory(final ExpirationPolicy expirationPolicy, final UniqueTicketIdGenerator idGenerator) {
        this.ticketGrantingTicketExpirationPolicy = expirationPolicy;
        this.tgtIdGenerator = idGenerator;
    }

    @Override
    public <T extends TicketGrantingTicket> T create(final Authentication authentication) {
        final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(this.tgtIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication, this.ticketGrantingTicketExpirationPolicy);
        return (T) ticketGrantingTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
