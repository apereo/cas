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
    private final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets. */
    private final ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    public DefaultProxyGrantingTicketFactory(final ExpirationPolicy expirationPolicy, final UniqueTicketIdGenerator idGenerator) {
        this.ticketGrantingTicketExpirationPolicy = expirationPolicy;
        this.ticketGrantingTicketUniqueTicketIdGenerator = idGenerator;
    }

    @Override
    public <T extends ProxyGrantingTicket> T create(final ServiceTicket serviceTicket, final Authentication authentication) throws AbstractTicketException {
        final String pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        final ProxyGrantingTicket proxyGrantingTicket = serviceTicket.grantProxyGrantingTicket(pgtId, authentication, ticketGrantingTicketExpirationPolicy);
        return (T) proxyGrantingTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
