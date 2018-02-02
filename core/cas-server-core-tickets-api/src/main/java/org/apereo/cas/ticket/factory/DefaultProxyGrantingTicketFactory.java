package org.apereo.cas.ticket.factory;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;

/**
 * The {@link DefaultProxyGrantingTicketFactory} is responsible
 * for creating {@link ProxyGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@AllArgsConstructor
public class DefaultProxyGrantingTicketFactory implements ProxyGrantingTicketFactory {
    /**
     * Used to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    protected UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /**
     * Expiration policy for ticket granting tickets.
     */
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /**
     * The ticket cipher.
     */
    protected CipherExecutor<String, String> cipherExecutor;

    @Override
    public <T extends ProxyGrantingTicket> T create(final ServiceTicket serviceTicket,
                                                    final Authentication authentication, final Class<T> clazz) throws AbstractTicketException {
        final String pgtId = produceTicketIdentifier();
        return produceTicket(serviceTicket, authentication, pgtId, clazz);
    }

    /**
     * Produce ticket.
     *
     * @param <T>            the type parameter
     * @param serviceTicket  the service ticket
     * @param authentication the authentication
     * @param pgtId          the pgt id
     * @param clazz          the clazz
     * @return the ticket
     */
    protected <T extends ProxyGrantingTicket> T produceTicket(final ServiceTicket serviceTicket, final Authentication authentication,
                                                              final String pgtId, final Class<T> clazz) {
        final ProxyGrantingTicket result = serviceTicket.grantProxyGrantingTicket(pgtId,
                authentication, this.ticketGrantingTicketExpirationPolicy);
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                + " is of type " + result.getClass()
                + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    /**
     * Produce ticket identifier string.
     *
     * @return the ticket
     */
    protected String produceTicketIdentifier() {
        String pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        if (this.cipherExecutor != null) {
            LOGGER.debug("Attempting to encode proxy-granting ticket [{}]", pgtId);
            pgtId = this.cipherExecutor.encode(pgtId);
            LOGGER.debug("Encoded proxy-granting ticket id [{}]", pgtId);
        }
        return pgtId;
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
