package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.AuthenticatedServicesAwareTicketGrantingTicket;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.google.common.io.ByteSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base ticket registry class that implements common ticket-related ops.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractTicketRegistry implements TicketRegistry {

    private static final String MESSAGE = "Ticket encryption is not enabled. Falling back to default behavior";

    /**
     * The cipher executor for ticket objects.
     */
    protected CipherExecutor cipherExecutor;

    /**
     * Gets principal id from ticket.
     *
     * @param ticket the ticket
     * @return the principal id from
     */
    protected static String getPrincipalIdFrom(final Ticket ticket) {
        return ticket instanceof AuthenticationAwareTicket
            ? Optional.ofNullable(((AuthenticationAwareTicket) ticket).getAuthentication())
            .map(auth -> auth.getPrincipal().getId()).orElse(StringUtils.EMPTY)
            : StringUtils.EMPTY;
    }

    @Override
    public void addTicket(final Ticket ticket) throws Exception {
        if (ticket != null && !ticket.isExpired()) {
            addTicketInternal(ticket);
        }
    }

    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final @NonNull Class<T> clazz) {
        val ticket = getTicket(ticketId);
        if (ticket == null) {
            LOGGER.debug("Ticket [{}] with type [{}] cannot be found", ticketId, clazz.getSimpleName());
            throw new InvalidTicketException(ticketId);
        }
        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId() + " is of type "
                                         + ticket.getClass() + " when we were expecting " + clazz);
        }
        return clazz.cast(ticket);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return getTicket(ticketId, ticket -> {
            if (ticket == null || ticket.isExpired()) {
                LOGGER.debug("Ticket [{}] has expired and will be removed from the ticket registry", ticketId);
                deleteSingleTicket(ticketId);
                return false;
            }
            return true;
        });
    }

    @Override
    public int deleteTicket(final String ticketId) throws Exception {
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
    public int deleteTicket(final Ticket ticket) throws Exception {
        val count = new AtomicLong(0);
        if (ticket instanceof TicketGrantingTicket tgt) {
            LOGGER.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            count.getAndAdd(deleteChildren(tgt));
            if (ticket instanceof ProxyGrantingTicket) {
                deleteProxyGrantingTicketFromParent((ProxyGrantingTicket) ticket);
            } else {
                deleteLinkedProxyGrantingTickets(count, tgt);
            }
        }
        LOGGER.debug("Removing ticket [{}] from the registry.", ticket);
        count.getAndAdd(deleteSingleTicket(ticket.getId()));
        return count.intValue();
    }

    @Override
    public long sessionCount() {
        try (val tgtStream = stream().filter(TicketGrantingTicket.class::isInstance)) {
            return tgtStream.count();
        } catch (final Exception t) {
            LOGGER.trace("sessionCount() operation is not implemented by the ticket registry instance [{}]. "
                         + "Message is: [{}] Returning unknown as [{}]", this.getClass().getName(), t.getMessage(), Long.MIN_VALUE);
            return Long.MIN_VALUE;
        }
    }

    @Override
    public long serviceTicketCount() {
        try (val stStream = stream().filter(ServiceTicket.class::isInstance)) {
            return stStream.count();
        } catch (final Exception t) {
            LOGGER.trace("serviceTicketCount() operation is not implemented by the ticket registry instance [{}]. "
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

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return true/false
     */
    public abstract long deleteSingleTicket(String ticketId);

    /**
     * Add ticket internally by the
     * registry implementation.
     *
     * @param ticket the ticket
     * @throws Exception the exception
     */
    protected abstract void addTicketInternal(Ticket ticket) throws Exception;

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
        return tickets.mapToInt(Unchecked.toIntFunction(this::deleteTicket)).sum();
    }

    /**
     * Delete ticket-granting ticket's service tickets.
     *
     * @param ticket the ticket
     * @return the count of tickets that were removed including child tickets and zero if the ticket was not deleted
     */
    protected int deleteChildren(final TicketGrantingTicket ticket) {
        val count = new AtomicLong(0);
        if (ticket instanceof AuthenticatedServicesAwareTicketGrantingTicket) {
            val services = ((AuthenticatedServicesAwareTicketGrantingTicket) ticket).getServices();
            if (services != null && !services.isEmpty()) {
                services.keySet().forEach(ticketId -> {
                    val deleteCount = deleteSingleTicket(ticketId);
                    if (deleteCount > 0) {
                        LOGGER.debug("Removed ticket [{}]", ticketId);
                        count.getAndAdd(deleteCount);
                    } else {
                        LOGGER.debug("Unable to remove ticket [{}]", ticketId);
                    }
                });
            }
        }
        return count.intValue();
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
        val encodedId = DigestUtils.sha512(ticketId);
        LOGGER.debug("Encoded original ticket id [{}] to [{}]", ticketId, encodedId);
        return encodedId;
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     * @throws Exception the exception
     */
    protected Ticket encodeTicket(final Ticket ticket) throws Exception {
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(MESSAGE);
            return ticket;
        }
        if (ticket == null) {
            LOGGER.debug("Ticket passed is null and cannot be encoded");
            return null;
        }
        val encodedTicket = createEncodedTicket(ticket);
        LOGGER.debug("Created encoded ticket [{}]", encodedTicket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param ticketToProcess the result
     * @return the ticket
     */
    protected Ticket decodeTicket(final Ticket ticketToProcess) {
        if (ticketToProcess instanceof EncodedTicket && !isCipherExecutorEnabled()) {
            LOGGER.warn("Found removable encoded ticket [{}] yet cipher operations are disabled. ", ticketToProcess.getId());
            deleteSingleTicket(ticketToProcess.getId());
            return null;
        }

        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(MESSAGE);
            return ticketToProcess;
        }
        if (ticketToProcess == null) {
            LOGGER.warn("Ticket passed is null and cannot be decoded");
            return null;
        }
        if (!(ticketToProcess instanceof EncodedTicket encodedTicket)) {
            LOGGER.warn("Ticket passed is not an encoded ticket: [{}], no decoding is necessary.",
                ticketToProcess.getClass().getSimpleName());
            return ticketToProcess;
        }
        LOGGER.debug("Attempting to decode [{}]", ticketToProcess);
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

    private Ticket createEncodedTicket(final Ticket ticket) throws Exception {
        LOGGER.debug("Encoding ticket [{}]", ticket);
        val encodedTicketObject = SerializationUtils.serializeAndEncodeObject(this.cipherExecutor, ticket);
        val encodedTicketId = encodeTicketId(ticket.getId());
        return new DefaultEncodedTicket(encodedTicketId,
            ByteSource.wrap(encodedTicketObject).read(), ticket.getPrefix());
    }

    private void deleteLinkedProxyGrantingTickets(final AtomicLong count,
                                                  final TicketGrantingTicket tgt) throws Exception {
        val pgts = new LinkedHashSet<>(tgt.getProxyGrantingTickets().keySet());
        val hasPgts = !pgts.isEmpty();
        count.getAndAdd(deleteTickets(pgts));
        if (hasPgts) {
            LOGGER.debug("Removing proxy-granting tickets from parent ticket-granting ticket");
            tgt.getProxyGrantingTickets().clear();
            updateTicket(tgt);
        }
    }

    private void deleteProxyGrantingTicketFromParent(final ProxyGrantingTicket ticket) throws Exception {
        ticket.getTicketGrantingTicket().getProxyGrantingTickets().remove(ticket.getId());
        updateTicket(ticket.getTicketGrantingTicket());
    }
}
