package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.pubsub.QueueableTicketRegistry;
import org.apereo.cas.ticket.registry.pubsub.commands.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.commands.DeleteTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.commands.DeleteTicketsMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.commands.UpdateTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link AbstractMapBasedTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public abstract class AbstractMapBasedTicketRegistry extends AbstractTicketRegistry implements QueueableTicketRegistry {

    protected final QueueableTicketRegistryMessagePublisher ticketPublisher;

    protected final PublisherIdentifier publisherIdentifier;

    public AbstractMapBasedTicketRegistry(final CipherExecutor cipherExecutor,
                                          final TicketSerializationManager ticketSerializationManager,
                                          final TicketCatalog ticketCatalog,
                                          final ApplicationContext applicationContext,
                                          final QueueableTicketRegistryMessagePublisher ticketPublisher,
                                          final PublisherIdentifier publisherIdentifier) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.ticketPublisher = ticketPublisher;
        this.publisherIdentifier = publisherIdentifier;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = digestIdentifier(ticketId);
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }
        val found = getMapInstance().get(encTicketId);
        if (found == null) {
            LOGGER.debug("Ticket [{}] could not be found", encTicketId);
            return null;
        }

        val result = decodeTicket(found);
        if (!predicate.test(result)) {
            LOGGER.debug("Cannot successfully fetch ticket [{}]", ticketId);
            return null;
        }
        return result;
    }

    @Override
    public long deleteAll() {
        val result = deleteAllFromQueue();
        if (ticketPublisher.isEnabled()) {
            ticketPublisher.publishMessageToQueue(new DeleteTicketsMessageQueueCommand(publisherIdentifier));
        }
        return result;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return decodeTickets(getMapInstance().values());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        val result = updateTicketInQueue(ticket);

        if (ticketPublisher.isEnabled()) {
            LOGGER.trace("Publishing update command for id [{}] and ticket [{}]", publisherIdentifier, ticket.getId());
            val command = new UpdateTicketMessageQueueCommand(publisherIdentifier, ticket);
            ticketPublisher.publishMessageToQueue(command);
        }
        return result;
    }

    @Override
    public long deleteSingleTicket(final Ticket ticket) {
        return deleteSingleTicket(ticket.getId());
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        val result = deleteTicketFromQueue(ticketId);
        if (ticketPublisher.isEnabled()) {
            LOGGER.trace("Publishing delete command for id [{}] and ticket [{}]", publisherIdentifier, ticketId);
            ticketPublisher.publishMessageToQueue(new DeleteTicketMessageQueueCommand(publisherIdentifier, ticketId));
        }
        return result;
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        addTicketToQueue(ticket);

        if (ticketPublisher.isEnabled()) {
            LOGGER.trace("Publishing add command for id [{}] and ticket [{}]", publisherIdentifier, ticket.getId());
            val command = new AddTicketMessageQueueCommand(publisherIdentifier, ticket);
            ticketPublisher.publishMessageToQueue(command);
        }
        return ticket;
    }

    @Override
    public void addTicketToQueue(final Ticket ticket) throws Exception {
        val encTicket = encodeTicket(ticket);
        LOGGER.debug("Putting ticket [{}] in registry.", ticket.getId());
        getMapInstance().put(encTicket.getId(), encTicket);
    }

    @Override
    public Ticket updateTicketInQueue(final Ticket ticket) throws Exception {
        LOGGER.trace("Updating ticket [{}] in registry...", ticket.getId());
        if (!ticket.isExpired()) {
            addTicketToQueue(ticket);
        }
        return ticket;
    }

    @Override
    public long deleteTicketFromQueue(final String ticketId) {
        val encTicketId = digestIdentifier(ticketId);
        return !StringUtils.isBlank(encTicketId) && getMapInstance().remove(encTicketId) != null ? 1 : 0;
    }

    @Override
    public long deleteAllFromQueue() {
        val size = getMapInstance().size();
        getMapInstance().clear();
        return size;
    }

    @Override
    public List<? extends Serializable> query(final TicketRegistryQueryCriteria criteria) {
        return getMapInstance()
            .values()
            .parallelStream()
            .filter(ticket -> criteria.getType().equals(ticket.getPrefix())
                && (StringUtils.isBlank(criteria.getId()) || digestIdentifier(criteria.getId()).equals(ticket.getId())))
            .map(ticket -> criteria.isDecode() ? decodeTicket(ticket) : ticket)
            .filter(ticket -> StringUtils.isBlank(criteria.getPrincipal())
                || (ticket instanceof final AuthenticationAwareTicket aat
                    && Strings.CI.equals(criteria.getPrincipal(), aat.getAuthentication().getPrincipal().getId())))
            .limit(criteria.getCount() > 0 ? criteria.getCount() : Long.MAX_VALUE)
            .collect(Collectors.toList());
    }

    /**
     * Count every ticket in the map, without reading any of them.
     *
     * @return the number of tickets
     */
    @Override
    public long countTickets() {
        return getMapInstance().size();
    }

    /**
     * Count the tickets of each type by their prefix, which a ticket keeps whether or not it is encoded, without
     * decoding or copying any of them.
     *
     * @return the number of tickets per prefix
     */
    @Override
    public Map<String, Long> countTicketsByType() {
        return getMapInstance()
            .values()
            .stream()
            .collect(Collectors.groupingBy(ticket -> Objects.toString(ticket.getPrefix(), "UNKNOWN"), TreeMap::new, Collectors.counting()));
    }

    @Override
    public long sessionCount() {
        return countTicketsOfType(TicketGrantingTicket.class);
    }

    @Override
    public long serviceTicketCount() {
        return countTicketsOfType(ServiceTicket.class);
    }

    /**
     * Count the tickets of a type without decoding or copying them: a ticket kept as is is checked by its class, and
     * an encoded ticket by its prefix, against the ticket definitions of that type. A count that cannot be made is
     * reported as unknown, the way the counts of other registries are.
     *
     * @param type the type of ticket
     * @return the number of tickets of that type, or {@link Long#MIN_VALUE} when it is unknown
     */
    protected long countTicketsOfType(final Class<? extends Ticket> type) {
        try {
            val prefixes = ticketCatalog.findTicketImplementations(type)
                .stream()
                .map(TicketDefinition::getPrefix)
                .collect(Collectors.toSet());
            return getMapInstance()
                .values()
                .stream()
                .filter(ticket -> ticket instanceof EncodedTicket ? prefixes.contains(ticket.getPrefix()) : type.isInstance(ticket))
                .count();
        } catch (final Exception e) {
            LOGGER.trace("Tickets of type [{}] could not be counted: [{}]", type.getSimpleName(), e.getMessage());
            return Long.MIN_VALUE;
        }
    }

    /**
     * Create map instance, which must ben created during initialization phases
     * and always be the same instance.
     *
     * @return the map
     */
    public abstract Map<String, Ticket> getMapInstance();
}
