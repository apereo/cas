package org.jasig.cas.ticket.registry.encrypt;


import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CipherExecutor;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.jasig.cas.util.CompressionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

/**
 * A distributed ticket registry that is able to encode
 * tickets before they are persisted and decoded back. This should be used, particularly
 * in clustered deployments where replication of tickets is carried out
 * over an insecure network connection. By default, encryption is turned off
 * unless {@link #setCipherExecutor(CipherExecutor)} is configured.
 *
 * @author Misagh Moayyed
 * @see org.jasig.cas.util.ShiroCipherExecutor
 * @since 4.2
 */
public abstract class AbstractCrypticTicketRegistry extends AbstractDistributedTicketRegistry {

    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor<byte[], byte[]> cipherExecutor;

    /**
     * Instantiates a new Cryptic ticket registry.
     */
    public AbstractCrypticTicketRegistry() {
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
