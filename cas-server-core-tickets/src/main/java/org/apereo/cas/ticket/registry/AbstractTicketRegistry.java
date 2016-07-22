package org.apereo.cas.ticket.registry;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server API.
 * </p>
 */
public abstract class AbstractTicketRegistry implements TicketRegistry {

    private static final String MESSAGE = "Ticket encryption is not enabled. Falling back to default behavior";
    
    protected transient Logger logger = LoggerFactory.getLogger(getClass());
    
    private CipherExecutor cipherExecutor;
    
    /**
     * Default constructor.
     */
    @SuppressWarnings("unchecked")
    public AbstractTicketRegistry() {
    }

    /**
     * {@inheritDoc}
     *
     * @return specified ticket from the registry
     * @throws IllegalArgumentException if class is null.
     * @throws ClassCastException       if class does not match requested ticket
     *                                  class.
     */
    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<T> clazz) {
        Assert.notNull(clazz, "clazz cannot be null");

        final Ticket ticket = this.getTicket(ticketId);

        if (ticket == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId()
                    + " is of type " + ticket.getClass()
                    + " when we were expecting " + clazz);
        }

        return (T) ticket;
    }

    @Override
    public long sessionCount() {
        logger.trace("sessionCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Long.MIN_VALUE);
        return Long.MIN_VALUE;
    }

    @Override
    public long serviceTicketCount() {
        logger.trace("serviceTicketCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Long.MIN_VALUE);
        return Long.MIN_VALUE;
    }

    @Override
    public boolean deleteTicket(final String ticketId) {
        if (ticketId == null) {
            return false;
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return false;
        }

        if (ticket instanceof TicketGrantingTicket) {
            if (ticket instanceof ProxyGrantingTicket) {
                logger.debug("Removing proxy-granting ticket [{}]", ticketId);
            }

            logger.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
            deleteChildren(tgt);

            final Collection<ProxyGrantingTicket> proxyGrantingTickets = tgt.getProxyGrantingTickets();
            proxyGrantingTickets.stream().map(Ticket::getId).forEach(this::deleteTicket);
        }
        logger.debug("Removing ticket [{}] from the registry.", ticket);
        return deleteSingleTicket(ticketId);
    }


    /**
     * Delete TGT's service tickets.
     *
     * @param ticket the ticket
     */
    public void deleteChildren(final TicketGrantingTicket ticket) {
        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            services.keySet().stream().forEach(ticketId -> {
                if (deleteSingleTicket(ticketId)) {
                    logger.debug("Removed ticket [{}]", ticketId);
                } else {
                    logger.debug("Unable to remove ticket [{}]", ticketId);
                }
            });
        }
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public boolean deleteSingleTicket(final Ticket ticketId) {
        return deleteSingleTicket(ticketId.getId());
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public abstract boolean deleteSingleTicket(final String ticketId);

    /**
     * Whether or not a callback to the TGT is required when checking for expiration.
     *
     * @return true, if successful
     */
    protected abstract boolean needsCallback();

    public void setCipherExecutor(final CipherExecutor<byte[], byte[]> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    /**
     * Encode ticket id into a SHA-512.
     *
     * @param ticketId the ticket id
     * @return the ticket
     */
    protected String encodeTicketId(final String ticketId) {
        if (!isCipherExecutorEnabled()) {
            logger.trace(MESSAGE);
            return ticketId;
        }
        if (StringUtils.isBlank(ticketId)) {
            return ticketId;
        }

        return DigestUtils.sha512(ticketId);
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    protected Ticket encodeTicket(final Ticket ticket) {
        if (!isCipherExecutorEnabled()) {
            logger.trace(MESSAGE);
            return ticket;
        }

        if (ticket == null) {
            logger.debug("Ticket passed is null and cannot be encoded");
            return null;
        }

        logger.info("Encoding [{}]", ticket);
        final byte[] encodedTicketObject = SerializationUtils.serializeAndEncodeObject(
                this.cipherExecutor, ticket);
        final String encodedTicketId = encodeTicketId(ticket.getId());
        final Ticket encodedTicket = new EncodedTicket(
                ByteSource.wrap(encodedTicketObject),
                encodedTicketId);
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
        if (!isCipherExecutorEnabled()) {
            logger.trace(MESSAGE);
            return result;
        }

        if (result == null) {
            logger.debug("Ticket passed is null and cannot be decoded");
            return null;
        }

        logger.info("Attempting to decode {}", result);
        final EncodedTicket encodedTicket = (EncodedTicket) result;

        final Ticket ticket = SerializationUtils.decodeAndSerializeObject(
                encodedTicket.getEncoded(), this.cipherExecutor, Ticket.class);
        logger.info("Decoded {}", ticket);
        return ticket;
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Collection<Ticket> decodeTickets(final Collection<Ticket> items) {
        if (!isCipherExecutorEnabled()) {
            logger.trace(MESSAGE);
            return items;
        }

        return items.stream().map(this::decodeTicket).collect(Collectors.toSet());
    }
    
    protected boolean isCipherExecutorEnabled() {
        return this.cipherExecutor != null && this.cipherExecutor.isEnabled();
    }
    
    
}
