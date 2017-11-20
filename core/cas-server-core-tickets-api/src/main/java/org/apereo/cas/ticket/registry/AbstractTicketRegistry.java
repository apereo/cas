package org.apereo.cas.ticket.registry;

import com.google.common.io.ByteSource;
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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server API.
 * </p>
 */
public abstract class AbstractTicketRegistry implements TicketRegistry {

    private static final String MESSAGE = "Ticket encryption is not enabled. Falling back to default behavior";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTicketRegistry.class);

    /**
     * The cipher executor for ticket objects.
     */
    protected CipherExecutor cipherExecutor;

    /**
     * Default constructor.
     */
    @SuppressWarnings("unchecked")
    public AbstractTicketRegistry() {
    }

    /**
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
            return getTickets().stream().filter(TicketGrantingTicket.class::isInstance).count();
        } catch (final Exception t) {
            LOGGER.trace("sessionCount() operation is not implemented by the ticket registry instance [{}]. "
                            + "Message is: [{}] Returning unknown as [{}]",
                    this.getClass().getName(), t.getMessage(), Long.MIN_VALUE);
            return Long.MIN_VALUE;
        }
    }

    @Override
    public long serviceTicketCount() {
        try {
            return getTickets().stream().filter(ServiceTicket.class::isInstance).count();
        } catch (final Exception t) {
            LOGGER.trace("serviceTicketCount() operation is not implemented by the ticket registry instance [{}]. "
                            + "Message is: [{}] Returning unknown as [{}]",
                    this.getClass().getName(), t.getMessage(), Long.MIN_VALUE);
            return Long.MIN_VALUE;
        }
    }

    @Override
    public int deleteTicket(final String ticketId) {
        final AtomicInteger count = new AtomicInteger(0);

        if (StringUtils.isBlank(ticketId)) {
            return count.intValue();
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return count.intValue();
        }

        if (ticket instanceof TicketGrantingTicket) {
            LOGGER.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
            count.addAndGet(deleteChildren(tgt));

            if (ticket instanceof ProxyGrantingTicket) {
                deleteProxyGrantingTicketFromParent((ProxyGrantingTicket) ticket);
            } else {
                deleteLinkedProxyGrantingTickets(count, tgt);
            }
        }

        LOGGER.debug("Removing ticket [{}] from the registry.", ticket);

        if (deleteSingleTicket(ticketId)) {
            count.incrementAndGet();
        }

        return count.intValue();
    }

    /**
     * Delete tickets.
     *
     * @param tickets the tickets
     * @return the total number of deleted tickets
     */
    protected int deleteTickets(final Set<String> tickets) {
        return deleteTickets(tickets.stream());
    }

    /**
     * Delete tickets.
     *
     * @param tickets the tickets
     * @return the total number of deleted tickets
     */
    protected int deleteTickets(final Stream<String> tickets) {
        return tickets.mapToInt(this::deleteTicket).sum();
    }
    
    private void deleteLinkedProxyGrantingTickets(final AtomicInteger count, final TicketGrantingTicket tgt) {
        final Set<String> pgts = new LinkedHashSet<>(tgt.getProxyGrantingTickets().keySet());
        final boolean hasPgts = !pgts.isEmpty();
        count.getAndAdd(deleteTickets(pgts));
        if (hasPgts) {
            LOGGER.debug("Removing proxy-granting tickets from parent ticket-granting ticket");
            tgt.getProxyGrantingTickets().clear();
            updateTicket(tgt);
        }
    }

    private void deleteProxyGrantingTicketFromParent(final ProxyGrantingTicket ticket) {
        final ProxyGrantingTicket thePgt = ticket;
        thePgt.getGrantingTicket().getProxyGrantingTickets().remove(thePgt.getId());
        updateTicket(thePgt.getGrantingTicket());
    }

    /**
     * Delete TGT's service tickets.
     *
     * @param ticket the ticket
     * @return the count of tickets that were removed including child tickets and zero if the ticket was not deleted
     */

    protected int deleteChildren(final TicketGrantingTicket ticket) {
        final AtomicInteger count = new AtomicInteger(0);

        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            services.keySet().stream().forEach(ticketId -> {
                if (deleteSingleTicket(ticketId)) {
                    LOGGER.debug("Removed ticket [{}]", ticketId);
                    count.incrementAndGet();
                } else {
                    LOGGER.debug("Unable to remove ticket [{}]", ticketId);
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
    public abstract boolean deleteSingleTicket(String ticketId);

    public void setCipherExecutor(final CipherExecutor cipherExecutor) {
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
            LOGGER.trace(MESSAGE);
            return ticketId;
        }
        if (StringUtils.isBlank(ticketId)) {
            return ticketId;
        }
        final String encodedId = DigestUtils.sha512(ticketId);
        LOGGER.debug("Encoded original ticket id [{}] to [{}]", ticketId, encodedId);
        return encodedId;
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    protected Ticket encodeTicket(final Ticket ticket) {
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(MESSAGE);
            return ticket;
        }

        if (ticket == null) {
            LOGGER.debug("Ticket passed is null and cannot be encoded");
            return null;
        }

        LOGGER.debug("Encoding ticket [{}]", ticket);
        final byte[] encodedTicketObject = SerializationUtils.serializeAndEncodeObject(this.cipherExecutor, ticket);
        final String encodedTicketId = encodeTicketId(ticket.getId());
        final Ticket encodedTicket = new EncodedTicket(ByteSource.wrap(encodedTicketObject), encodedTicketId);
        LOGGER.debug("Created encoded ticket [{}]", encodedTicket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param result the result
     * @return the ticket
     */
    protected Ticket decodeTicket(final Ticket result) {
        try {
            if (!isCipherExecutorEnabled()) {
                LOGGER.trace(MESSAGE);
                return result;
            }

            if (result == null) {
                LOGGER.warn("Ticket passed is null and cannot be decoded");
                return null;
            }
            if (!result.getClass().isAssignableFrom(EncodedTicket.class)) {
                LOGGER.warn("Ticket passed is not an encoded ticket type; rather it's a [{}], no decoding is necessary.",
                        result.getClass().getSimpleName());
                return result;
            }

            LOGGER.debug("Attempting to decode [{}]", result);
            final EncodedTicket encodedTicket = (EncodedTicket) result;

            final Ticket ticket = SerializationUtils.decodeAndDeserializeObject(
                    encodedTicket.getEncoded(), this.cipherExecutor, Ticket.class);
            LOGGER.debug("Decoded ticket to [{}]", ticket);
            return ticket;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Collection<Ticket> decodeTickets(final Collection<Ticket> items) {
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(MESSAGE);
            return items;
        }

        return items
                .stream()
                .map(this::decodeTicket)
                .collect(Collectors.toSet());
    }

    protected boolean isCipherExecutorEnabled() {
        return this.cipherExecutor != null && this.cipherExecutor.isEnabled();
    }


}
