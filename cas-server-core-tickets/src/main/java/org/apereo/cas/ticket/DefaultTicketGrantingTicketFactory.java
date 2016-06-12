package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link TicketGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultTicketGrantingTicketFactory implements TicketGrantingTicketFactory {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * UniqueTicketIdGenerator to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    
    @Autowired
    @Qualifier("ticketGrantingTicketUniqueIdGenerator")
    protected UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets. */
    
    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    @Override
    public <T extends TicketGrantingTicket> T create(final Authentication authentication) {
        final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
                this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication, this.ticketGrantingTicketExpirationPolicy);
        return (T) ticketGrantingTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public void setTicketGrantingTicketUniqueTicketIdGenerator(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator) {
        this.ticketGrantingTicketUniqueTicketIdGenerator = ticketGrantingTicketUniqueTicketIdGenerator;
    }

    public void setTicketGrantingTicketExpirationPolicy(final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
    }
}
