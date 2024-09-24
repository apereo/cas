package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import com.google.common.io.ByteSource;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ApplicationContext;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
@AllArgsConstructor
public abstract class AbstractTicketRegistry implements TicketRegistry {

    private static final String TICKET_ENCRYPTION_LOG_MESSAGE = "Ticket encryption is not enabled. Falling back to default behavior";

    @Setter
    protected CipherExecutor cipherExecutor;

    protected final TicketSerializationManager ticketSerializationManager;

    protected final TicketCatalog ticketCatalog;

    protected final ApplicationContext applicationContext;

    protected static String getPrincipalIdFrom(final Ticket ticket) {
        return ticket instanceof AuthenticationAwareTicket
            ? Optional.ofNullable(((AuthenticationAwareTicket) ticket).getAuthentication())
            .map(auth -> auth.getPrincipal().getId()).orElse(StringUtils.EMPTY)
            : StringUtils.EMPTY;
    }

    protected Map collectAndDigestTicketAttributes(final Ticket ticket) {
        val currentAttributes = getCombinedTicketAttributes(ticket);
        if (isCipherExecutorEnabled()) {
            val encodedAttributes = new HashMap<String, Object>(currentAttributes.size());
            currentAttributes.forEach((key, value) -> encodedAttributes.put(digestIdentifier(key), digestIdentifier(value)));
            return encodedAttributes;
        }
        return currentAttributes;
    }

    private static Map<String, List<Object>> getCombinedTicketAttributes(final Ticket ticket) {
        if (ticket instanceof final AuthenticationAwareTicket authnTicket) {
            val authentication = authnTicket.getAuthentication();
            if (authentication != null) {
                val attributes = new HashMap<>(authentication.getAttributes());
                val principal = authentication.getPrincipal();
                return CoreAuthenticationUtils.mergeAttributes(attributes, principal.getAttributes());
            }
        }
        return new HashMap<>();
    }

    @Override
    public Ticket addTicket(final Ticket ticket) throws Exception {
        return ticket != null && !ticket.isExpired() ? addSingleTicket(ticket) : null;
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        val returnTicket = getTicket(ticketId, ticket -> {
            if (ticket.isExpired()) {
                val ticketAgeSeconds = getTicketAgeSeconds(ticket);
                LOGGER.debug("Ticket [{}] has expired according to policy [{}] after [{}] seconds and [{}] uses and will be removed from the ticket registry",
                    ticketId, ticket.getExpirationPolicy().getName(), ticketAgeSeconds, ticket.getCountOfUses());
                if (ticket instanceof final TicketGrantingTicket tgt) {
                    val clientInfo = ClientInfoHolder.getClientInfo();
                    applicationContext.publishEvent(new CasRequestSingleLogoutEvent(this, tgt, clientInfo));
                    applicationContext.publishEvent(new CasTicketGrantingTicketDestroyedEvent(this, tgt, clientInfo));
                }
                try {
                    deleteTicket(ticket);
                } catch (final Exception e) {
                    LoggingUtils.warn(LOGGER, e);
                }
                return false;
            }
            return true;
        });
        if (returnTicket != null) {
            val ticketAgeSeconds = getTicketAgeSeconds(returnTicket);
            if (ticketAgeSeconds < -1) {
                LOGGER.warn("Ticket created [{}] second(s) in the future. Check time synchronization on all servers.", ticketAgeSeconds * -1);
            }
        }
        return returnTicket;
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
        if (ticket instanceof final TicketGrantingTicket tgt) {
            LOGGER.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            count.getAndAdd(deleteChildren(tgt));
            if (ticket instanceof final ProxyGrantingTicket pgt) {
                deleteProxyGrantingTicketFromParent(pgt);
            } else {
                deleteLinkedProxyGrantingTickets(count, tgt);
            }
        }
        LOGGER.debug("Removing ticket [{}] from the registry.", ticket);
        count.getAndAdd(deleteSingleTicket(ticket));
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
            if (t instanceof final TicketGrantingTicket ticket) {
                return ticket.getAuthentication().getPrincipal().getId().equalsIgnoreCase(principalId);
            }
            return false;
        };
        return getTickets(ticketPredicate).count();
    }

    @Override
    public String digestIdentifier(final String identifier) {
        if (StringUtils.isBlank(identifier)) {
            return identifier;
        }
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(TICKET_ENCRYPTION_LOG_MESSAGE);
            return identifier;
        }
        val encodedId = DigestUtils.sha512(identifier);
        LOGGER.debug("Digested original ticket id [{}] to [{}]", identifier, encodedId);
        return encodedId;
    }

    protected List<String> digestIdentifier(final Collection<Object> identifiers) {
        return identifiers
            .stream()
            .map(Object::toString)
            .map(this::digestIdentifier)
            .collect(Collectors.toList());
    }

    @Override
    public long countTicketsFor(final Service service) {
        return stream()
            .map(this::decodeTicket)
            .filter(ServiceAwareTicket.class::isInstance)
            .filter(ticket -> !ticket.isExpired())
            .map(ServiceAwareTicket.class::cast)
            .filter(ticket -> Objects.nonNull(ticket.getService()))
            .filter(ticket -> ticket.getService().getId().equals(service.getId()))
            .count();
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        return getTickets(ticket -> {
            if (ticket instanceof final TicketGrantingTicket ticketGrantingTicket && !ticket.isExpired()
                && ticketGrantingTicket.getAuthentication() != null) {
                val attributes = collectAndDigestTicketAttributes(ticketGrantingTicket);

                return queryAttributes.entrySet().stream().anyMatch(queryEntry -> {
                    val attributeKey = digestIdentifier(queryEntry.getKey());

                    if (attributes.containsKey(attributeKey)) {

                        val authnAttributeValues = CollectionUtils.toCollection(attributes.get(attributeKey));

                        return authnAttributeValues.stream().anyMatch(value -> {
                            val attributeValue = value.toString();
                            return queryEntry.getValue()
                                .stream()
                                .map(queryValue -> digestIdentifier(queryValue.toString()))
                                .anyMatch(attributeValue::equalsIgnoreCase);
                        });
                    }
                    return false;
                });
            }
            return false;
        });
    }

    protected long deleteSingleTicket(final Ticket ticket) {
        return 0;
    }

    protected abstract Ticket addSingleTicket(Ticket ticket) throws Exception;

    protected int deleteTickets(final Set<String> tickets) {
        return deleteTickets(tickets.stream());
    }

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
        val services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            services.keySet()
                .stream()
                .map(this::getTicket)
                .filter(Objects::nonNull)
                .forEach(serviceTicket -> {
                    val deleteCount = deleteSingleTicket(serviceTicket);
                    if (deleteCount > 0) {
                        LOGGER.debug("Removed ticket [{}]", serviceTicket.getId());
                        count.getAndAdd(deleteCount);
                    } else {
                        LOGGER.debug("Unable to remove ticket [{}]", serviceTicket.getId());
                    }
                });
        }
        return count.intValue();
    }

    protected Ticket encodeTicket(final Ticket ticket) throws Exception {
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(TICKET_ENCRYPTION_LOG_MESSAGE);
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

    protected Ticket decodeTicket(final Ticket ticketToProcess) {
        if (ticketToProcess instanceof EncodedTicket && !isCipherExecutorEnabled()) {
            LOGGER.warn("Found removable encoded ticket [{}] yet cipher operations are disabled.", ticketToProcess.getId());
            FunctionUtils.doUnchecked(__ -> deleteTicket(ticketToProcess));
            return null;
        }

        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(TICKET_ENCRYPTION_LOG_MESSAGE);
            return ticketToProcess;
        }
        if (ticketToProcess == null) {
            LOGGER.warn("Ticket passed is null and cannot be decoded");
            return null;
        }
        if (!(ticketToProcess instanceof final EncodedTicket encodedTicket)) {
            LOGGER.debug("Ticket passed is not an encoded ticket: [{}], no decoding is necessary.",
                ticketToProcess.getClass().getSimpleName());
            return ticketToProcess;
        }
        LOGGER.debug("Attempting to decode [{}]", ticketToProcess);
        val ticket = SerializationUtils.decodeAndDeserializeObject(encodedTicket.getEncodedTicket(), this.cipherExecutor, Ticket.class);
        LOGGER.debug("Decoded ticket to [{}]", ticket);
        return ticket;
    }

    protected Collection<Ticket> decodeTickets(final Collection<Ticket> items) {
        return decodeTickets(items.stream()).collect(Collectors.toSet());
    }

    protected Stream<Ticket> decodeTickets(final Stream<Ticket> items) {
        if (!isCipherExecutorEnabled()) {
            LOGGER.trace(TICKET_ENCRYPTION_LOG_MESSAGE);
            return items;
        }
        return items.map(this::decodeTicket);
    }

    protected boolean isCipherExecutorEnabled() {
        return this.cipherExecutor != null && this.cipherExecutor.isEnabled();
    }

    protected String serializeTicket(final Ticket ticket) {
        return ticketSerializationManager.serializeTicket(ticket);
    }

    protected Ticket createEncodedTicket(final Ticket ticket) throws Exception {
        LOGGER.debug("Encoding ticket [{}]", ticket);
        val encodedTicketObject = SerializationUtils.serializeAndEncodeObject(cipherExecutor, ticket);
        return toEncodedTicket(ticket, encodedTicketObject);
    }

    protected Ticket toEncodedTicket(final Ticket ticket, final byte[] encodedTicketObject) throws Exception {
        val encodedTicketId = digestIdentifier(ticket.getId());
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
        if (ticket.getTicketGrantingTicket() instanceof final TicketGrantingTicket tgt) {
            tgt.getProxyGrantingTickets().remove(ticket.getId());
            updateTicket(tgt);
        }
    }

    private static long getTicketAgeSeconds(final @NonNull Ticket ticket) {
        return ZonedDateTime.now(ticket.getExpirationPolicy().getClock()).toEpochSecond() - ticket.getCreationTime().toEpochSecond();
    }

    protected Ticket deserializeTicket(final String ticketContent, final String type) {
        return ticketSerializationManager.deserializeTicket(ticketContent, type);
    }
}
