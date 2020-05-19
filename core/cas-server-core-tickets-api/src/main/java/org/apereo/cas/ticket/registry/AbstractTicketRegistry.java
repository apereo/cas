package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.google.common.io.ByteSource;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server API.
 * </p>
 */
@Slf4j
@Setter
@NoArgsConstructor
public abstract class AbstractTicketRegistry implements TicketRegistry {

    private static final String MESSAGE = "Ticket encryption is not enabled. Falling back to default behavior";

    /**
     * The cipher executor for ticket objects.
     */
    protected CipherExecutor cipherExecutor;

    @Override
    public Ticket getTicket(final String ticketId) {
        return getTicket(ticketId, ticket -> {
            if (ticket != null && ticket.isExpired()) {
                LOGGER.debug("Ticket [{}] has expired and is now removed from the ticket registry", ticket.getId());
                deleteSingleTicket(ticketId);
                return false;
            }
            return true;
        });
    }

    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final @NonNull Class<T> clazz) {
        val ticket = getTicket(ticketId);
        if (ticket == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId() + " is of type " + ticket.getClass() + " when we were expecting " + clazz);
        }
        return clazz.cast(ticket);
    }

    @Override
    public long sessionCount() {
        try (val tgtStream = getTicketsStream().filter(TicketGrantingTicket.class::isInstance)) {
            return tgtStream.count();
        } catch (final Exception t) {
            LOGGER.trace("sessionCount() operation is not implemented by the ticket registry instance [{}]. "
                + "Message is: [{}] Returning unknown as [{}]", this.getClass().getName(), t.getMessage(), Long.MIN_VALUE);
            return Long.MIN_VALUE;
        }
    }

    @Override
    public long countSessionsFor(final String principalId) {
        val ticketPredicate = (Predicate<Ticket>) t -> {
            if (t instanceof TicketGrantingTicket) {
                val ticket = TicketGrantingTicket.class.cast(t);
                return ticket.getAuthentication().getPrincipal().getId().equalsIgnoreCase(principalId);
            }
            return false;
        };
        return getTickets(ticketPredicate).count();
    }
    
    @Override
    public long serviceTicketCount() {
        try (val stStream = getTicketsStream().filter(ServiceTicket.class::isInstance)) {
            return stStream.count();
        } catch (final Exception t) {
            LOGGER.trace("serviceTicketCount() operation is not implemented by the ticket registry instance [{}]. "
                + "Message is: [{}] Returning unknown as [{}]", this.getClass().getName(), t.getMessage(), Long.MIN_VALUE);
            return Long.MIN_VALUE;
        }
    }

    @Override
    public int deleteTicket(final String ticketId) {
        if (StringUtils.isBlank(ticketId)) {
            LOGGER.trace("No ticket id is provided for deletion");
            return 0;
        }
        val ticket = getTicket(ticketId);
        if (ticket == null) {
            LOGGER.debug("Ticket [{}] could not be fetched from the registry; it may have been expired and deleted.", ticketId);
            return 0;
        }
        return deleteTicket(ticket);
    }

    @Override
    public int deleteTicket(final Ticket ticket) {
        val count = new AtomicInteger(0);
        if (ticket instanceof TicketGrantingTicket) {
            LOGGER.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            val tgt = (TicketGrantingTicket) ticket;
            count.addAndGet(deleteChildren(tgt));
            if (ticket instanceof ProxyGrantingTicket) {
                deleteProxyGrantingTicketFromParent((ProxyGrantingTicket) ticket);
            } else {
                deleteLinkedProxyGrantingTickets(count, tgt);
            }
        }
        LOGGER.debug("Removing ticket [{}] from the registry.", ticket);
        if (deleteSingleTicket(ticket.getId())) {
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
        val pgts = new LinkedHashSet<String>(tgt.getProxyGrantingTickets().keySet());
        val hasPgts = !pgts.isEmpty();
        count.getAndAdd(deleteTickets(pgts));
        if (hasPgts) {
            LOGGER.debug("Removing proxy-granting tickets from parent ticket-granting ticket");
            tgt.getProxyGrantingTickets().clear();
            updateTicket(tgt);
        }
    }

    private void deleteProxyGrantingTicketFromParent(final ProxyGrantingTicket ticket) {
        ticket.getTicketGrantingTicket().getProxyGrantingTickets().remove(ticket.getId());
        updateTicket(ticket.getTicketGrantingTicket());
    }

    /**
     * Delete ticket-granting ticket's service tickets.
     *
     * @param ticket the ticket
     * @return the count of tickets that were removed including child tickets and zero if the ticket was not deleted
     */
    protected int deleteChildren(final TicketGrantingTicket ticket) {
        val count = new AtomicInteger(0);
        val services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            services.keySet().forEach(ticketId -> {
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
        val encodedId = DigestUtils.sha512(ticketId);
        LOGGER.debug("Encoded original ticket id [{}] to [{}]", ticketId, encodedId);
        return encodedId;
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    @SneakyThrows
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
        val encodedTicketObject = SerializationUtils.serializeAndEncodeObject(this.cipherExecutor, ticket);
        val encodedTicketId = encodeTicketId(ticket.getId());
        val encodedTicket = new EncodedTicket(encodedTicketId,
            ByteSource.wrap(encodedTicketObject).read(), ticket.getPrefix());
        LOGGER.debug("Created encoded ticket [{}]", encodedTicket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param result the result
     * @return the ticket
     */
    @SneakyThrows
    protected Ticket decodeTicket(final Ticket result) {
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(MESSAGE);
            return result;
        }
        if (result == null) {
            LOGGER.warn("Ticket passed is null and cannot be decoded");
            return null;
        }
        if (!result.getClass().isAssignableFrom(EncodedTicket.class)) {
            LOGGER.warn("Ticket passed is not an encoded ticket type; rather it's a [{}], no decoding is necessary.", result.getClass().getSimpleName());
            return result;
        }
        LOGGER.debug("Attempting to decode [{}]", result);
        val encodedTicket = (EncodedTicket) result;
        val ticket = SerializationUtils.decodeAndDeserializeObject(encodedTicket.getEncodedTicket(), this.cipherExecutor, Ticket.class);
        LOGGER.debug("Decoded ticket to [{}]", ticket);
        return ticket;
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Collection<Ticket> decodeTickets(final Collection<Ticket> items) {
        return decodeTickets(items.stream()).collect(Collectors.toSet());
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Stream<Ticket> decodeTickets(final Stream<Ticket> items) {
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(MESSAGE);
            return items;
        }
        return items.map(this::decodeTicket);
    }

    protected boolean isCipherExecutorEnabled() {
        return this.cipherExecutor != null && this.cipherExecutor.isEnabled();
    }
}
