package org.jasig.cas.ticket.registry;

import org.jasig.cas.CipherExecutor;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.util.CompressionUtils;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Abstract Implementation that handles some of the commonalities between
 * distributed ticket registries.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public abstract class AbstractDistributedTicketRegistry extends AbstractTicketRegistry {

    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor<byte[], byte[]> cipherExecutor;

    /**
     * Update the received ticket.
     *
     * @param ticket the ticket
     */
    protected abstract void updateTicket(Ticket ticket);

    /**
     * Whether or not a callback to the TGT is required when checking for expiration.
     *
     * @return true, if successful
     */
    protected abstract boolean needsCallback();

    /**
     * Gets the proxied ticket instance.
     *
     * @param ticket the ticket
     * @return the proxied ticket instance
     */
    protected final Ticket getProxiedTicketInstance(final Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        if (ticket instanceof ProxyGrantingTicket) {
            return new ProxyGrantingTicketDelegator(this, (ProxyGrantingTicket) ticket, needsCallback());
        }

        if (ticket instanceof TicketGrantingTicket) {
            return new TicketGrantingTicketDelegator<>(this, (TicketGrantingTicket) ticket, needsCallback());
        }

        if (ticket instanceof ProxyTicket) {
            return new ProxyTicketDelegator(this, (ProxyTicket) ticket, needsCallback());
        }

        if (ticket instanceof ServiceTicket) {
            return new ServiceTicketDelegator<>(this, (ServiceTicket) ticket, needsCallback());
        }

        throw new IllegalStateException("Cannot wrap ticket of type: " + ticket.getClass() + " with a proxy delegator");
    }

    public void setCipherExecutor(final CipherExecutor<byte[], byte[]> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    /**
     * Encode ticket id into a SHA-512.
     *
     * @param ticketId the ticket id
     * @return the ticket
     */
    protected String encodeTicketId(final String ticketId)  {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return ticketId;
        }
        if (StringUtils.isBlank(ticketId)) {
            return ticketId;
        }

        return CompressionUtils.sha512Hex(ticketId);
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    protected Ticket encodeTicket(final Ticket ticket)  {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return ticket;
        }

        if (ticket == null) {
            return ticket;
        }

        logger.info("Encoding [{}]", ticket);
        final byte[] encodedTicketObject = CompressionUtils.serializeAndEncodeObject(
                this.cipherExecutor, ticket);
        final String encodedTicketId = encodeTicketId(ticket.getId());
        final Ticket encodedTicket = new EncodedTicket(
                ByteSource.wrap(encodedTicketObject), encodedTicketId);
        logger.info("Created [{}]", encodedTicket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param result the result
     * @return the ticket
     */
    protected Ticket decodeTicket(final Ticket result) {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return result;
        }

        if (result == null) {
            return result;
        }

        logger.info("Attempting to decode {}", result);
        final EncodedTicket encodedTicket = (EncodedTicket) result;

        final Ticket ticket = CompressionUtils.decodeAndSerializeObject(
                encodedTicket.getEncoded(), this.cipherExecutor, Ticket.class);
        logger.info("Decoded {}",  ticket);
        return ticket;
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Collection<Ticket> decodeTickets(final Collection<Ticket> items) {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return items;
        }

        if (items == null || items.isEmpty()) {
            return items;
        }

        final Collection<Ticket> tickets = new HashSet<>(items.size());
        for (final Ticket item : items) {
            final Ticket ticket = decodeTicket(item);
            tickets.add(ticket);
        }
        return tickets;
    }
}
