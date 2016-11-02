package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link ProxyGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultProxyGrantingTicketFactory implements ProxyGrantingTicketFactory {

    /**
     * Used to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    protected UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets. */
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    @Override
    public <T extends ProxyGrantingTicket> T create(final ServiceTicket serviceTicket,
                                                    final Authentication authentication)
                                                    throws AbstractTicketException {
        final String pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(
                ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        final ProxyGrantingTicket proxyGrantingTicket = serviceTicket.grantProxyGrantingTicket(pgtId,
                authentication, this.ticketGrantingTicketExpirationPolicy);
        return (T) proxyGrantingTicket;
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
