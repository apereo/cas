package org.apereo.cas.ticket;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link ProxyGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultProxyGrantingTicketFactory implements ProxyGrantingTicketFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProxyGrantingTicketFactory.class);

    /**
     * Used to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    protected UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /**
     * Expiration policy for ticket granting tickets.
     */
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    private CipherExecutor<String, String> cipherExecutor;

    @Override
    public <T extends ProxyGrantingTicket> T create(final ServiceTicket serviceTicket,
                                                    final Authentication authentication) throws AbstractTicketException {
        String pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        if (this.cipherExecutor != null) {
            LOGGER.debug("Attempting to encode proxy-granting ticket {}", pgtId);
            pgtId = this.cipherExecutor.encode(pgtId);
            LOGGER.debug("Encoded proxy-granting ticket id {}", pgtId);
        }
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

    public void setCipherExecutor(final CipherExecutor<String, String> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }
}
