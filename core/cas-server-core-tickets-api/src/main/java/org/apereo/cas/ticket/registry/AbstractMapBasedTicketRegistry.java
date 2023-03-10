package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
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

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

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
                                          final QueueableTicketRegistryMessagePublisher ticketPublisher,
                                          final PublisherIdentifier publisherIdentifier) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.ticketPublisher = ticketPublisher;
        this.publisherIdentifier = publisherIdentifier;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = digest(ticketId);
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
    public long deleteSingleTicket(final String ticketId) {
        val result = deleteTicketFromQueue(ticketId);
        if (ticketPublisher.isEnabled()) {
            LOGGER.trace("Publishing delete command for id [{}] and ticket [{}]", publisherIdentifier, ticketId);
            ticketPublisher.publishMessageToQueue(new DeleteTicketMessageQueueCommand(publisherIdentifier, ticketId));
        }
        return result;
    }

    @Override
    public void addTicketInternal(final Ticket ticket) throws Exception {
        addTicketToQueue(ticket);

        if (ticketPublisher.isEnabled()) {
            LOGGER.trace("Publishing add command for id [{}] and ticket [{}]", publisherIdentifier, ticket.getId());
            val command = new AddTicketMessageQueueCommand(publisherIdentifier, ticket);
            ticketPublisher.publishMessageToQueue(command);
        }
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
        addTicket(ticket);
        return ticket;
    }

    @Override
    public long deleteTicketFromQueue(final String ticketId) {
        val encTicketId = digest(ticketId);
        return !StringUtils.isBlank(encTicketId) && getMapInstance().remove(encTicketId) != null ? 1 : 0;
    }

    @Override
    public long deleteAllFromQueue() {
        val size = getMapInstance().size();
        getMapInstance().clear();
        return size;
    }

    /**
     * Create map instance, which must ben created during initialization phases
     * and always be the same instance.
     *
     * @return the map
     */
    public abstract Map<String, Ticket> getMapInstance();
}
