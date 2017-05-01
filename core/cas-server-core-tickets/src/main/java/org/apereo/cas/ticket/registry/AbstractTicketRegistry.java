package org.apereo.cas.ticket.registry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.io.ByteSource;

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
        try {
            return getTickets().stream().filter(t -> t instanceof TicketGrantingTicket).count();
        } catch (final Throwable t) {
            logger.trace("sessionCount() operation is not implemented by the ticket registry instance {}. "
                            + "Message is: {} Returning unknown as {}",
                    this.getClass().getName(), t.getMessage(), Long.MIN_VALUE);
            return Long.MIN_VALUE;
        }
    }

    @Override
    public long serviceTicketCount() {
        try {
            return getTickets().stream().filter(t -> t instanceof ServiceTicket).count();
        } catch (final Throwable t) {
            logger.trace("serviceTicketCount() operation is not implemented by the ticket registry instance {}. "
                            + "Message is: {} Returning unknown as {}",
                    this.getClass().getName(), t.getMessage(), Long.MIN_VALUE);
            return Long.MIN_VALUE;
        }
    }

    @Override
    public int deleteTicket(final String ticketId) {
        final AtomicInteger count = new AtomicInteger(0);

        if (ticketId == null) {
            return count.intValue();
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return count.intValue();
        }

        if (ticket instanceof TicketGrantingTicket) {
            if (ticket instanceof ProxyGrantingTicket) {
                logger.debug("Removing proxy-granting ticket [{}]", ticketId);
            }

            logger.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
            count.addAndGet(deleteChildren(tgt));

            final Collection<ProxyGrantingTicket> proxyGrantingTickets = tgt.getProxyGrantingTickets();
            proxyGrantingTickets.stream().map(Ticket::getId).forEach((t) -> {
                count.addAndGet(this.deleteTicket(t));
            });
        }
        logger.debug("Removing ticket [{}] from the registry.", ticket);

        if (deleteSingleTicket(ticketId)) {
            count.incrementAndGet();
        }

        return count.intValue();
    }


    /**
     * Delete TGT's service tickets.
     *
     * @param ticket the ticket
     * @return the count of tickets that were removed including child tickets and zero if the ticket was not deleted
     */
    public int deleteChildren(final TicketGrantingTicket ticket) {
        final AtomicInteger count = new AtomicInteger(0);

        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            services.keySet().stream().forEach(ticketId -> {
                if (deleteSingleTicket(ticketId)) {
                    logger.debug("Removed ticket [{}]", ticketId);
                    count.incrementAndGet();
                } else {
                    logger.debug("Unable to remove ticket [{}]", ticketId);
                }
            });
        }

        return count.intValue();
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return true/false
     */
    public boolean deleteSingleTicket(final Ticket ticketId) {
        return deleteSingleTicket(ticketId.getId());
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return true/false
     */
    public abstract boolean deleteSingleTicket(String ticketId);

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

        logger.debug("Attempting to decode {}", result);
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
