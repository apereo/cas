package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link TicketGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTicketGrantingTicketFactory implements TicketGrantingTicketFactory {
    /**
     * UniqueTicketIdGenerator to generate ids for {@link TicketGrantingTicket}s created.
     */
    protected final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /**
     * Expiration policy for ticket granting tickets.
     */
    protected final ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicy;

    /**
     * The ticket cipher, if any.
     */
    protected final CipherExecutor<Serializable, String> cipherExecutor;

    @Override
    public <T extends TicketGrantingTicket> T create(final Authentication authentication, final Class<T> clazz) {
        val tgtId = produceTicketIdentifier(authentication);
        return produceTicket(authentication, tgtId, clazz);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }

    /**
     * Produce ticket.
     *
     * @param <T>            the type parameter
     * @param authentication the authentication
     * @param tgtId          the tgt id
     * @param clazz          the clazz
     * @return the ticket.
     */
    protected <T extends TicketGrantingTicket> T produceTicket(final Authentication authentication,
                                                               final String tgtId, final Class<T> clazz) {
        val result = new TicketGrantingTicketImpl(tgtId, authentication,
            this.ticketGrantingTicketExpirationPolicy.buildTicketExpirationPolicy());
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
     * @param authentication the authentication
     * @return the ticket id.
     */
    protected String produceTicketIdentifier(final Authentication authentication) {
        var tgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
        if (this.cipherExecutor != null) {
            LOGGER.trace("Attempting to encode ticket-granting ticket [{}]", tgtId);
            tgtId = this.cipherExecutor.encode(tgtId);
            LOGGER.trace("Encoded ticket-granting ticket id [{}]", tgtId);
        }
        return tgtId;
    }
}
