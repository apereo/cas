package org.apereo.cas.ticket.factory;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link TicketGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultTicketGrantingTicketFactory implements TicketGrantingTicketFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTicketGrantingTicketFactory.class);

    /**
     * UniqueTicketIdGenerator to generate ids for {@link TicketGrantingTicket}s created.
     */
    protected UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /**
     * Expiration policy for ticket granting tickets.
     */
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /**
     * The ticket cipher, if any.
     */
    protected CipherExecutor<Serializable, String> cipherExecutor;

    public DefaultTicketGrantingTicketFactory(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                                              final ExpirationPolicy ticketGrantingTicketExpirationPolicy,
                                              final CipherExecutor<Serializable, String> cipherExecutor) {
        this.ticketGrantingTicketUniqueTicketIdGenerator = ticketGrantingTicketUniqueTicketIdGenerator;
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public <T extends TicketGrantingTicket> T create(final Authentication authentication) {
        final String tgtId = produceTicketIdentifier(authentication);
        return produceTicket(authentication, tgtId);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    /**
     * Produce ticket.
     *
     * @param <T>            the type parameter
     * @param authentication the authentication
     * @param tgtId          the tgt id
     * @return the ticket.
     */
    protected <T extends TicketGrantingTicket> T produceTicket(final Authentication authentication, final String tgtId) {
        final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
                tgtId, authentication, this.ticketGrantingTicketExpirationPolicy);
        return (T) ticketGrantingTicket;
    }

    /**
     * Produce ticket identifier string.
     *
     * @param authentication the authentication
     * @return the ticket id.
     */
    protected String produceTicketIdentifier(final Authentication authentication) {
        String tgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
        if (this.cipherExecutor != null) {
            LOGGER.debug("Attempting to encode ticket-granting ticket [{}]", tgtId);
            tgtId = this.cipherExecutor.encode(tgtId);
            LOGGER.debug("Encoded ticket-granting ticket id [{}]", tgtId);
        }
        return tgtId;
    }
}
