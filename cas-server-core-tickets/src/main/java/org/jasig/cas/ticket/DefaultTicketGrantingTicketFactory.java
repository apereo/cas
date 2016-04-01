package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link TicketGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("defaultTicketGrantingTicketFactory")
public class DefaultTicketGrantingTicketFactory implements TicketGrantingTicketFactory {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * UniqueTicketIdGenerator to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    @NotNull
    @Resource(name="ticketGrantingTicketUniqueIdGenerator")
    protected UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets. */
    @NotNull
    @Resource(name="grantingTicketExpirationPolicy")
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    @Override
    public <T extends TicketGrantingTicket> T create(final Authentication authentication) {
        final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
                this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication, ticketGrantingTicketExpirationPolicy);
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
