package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.queue.TicketRegistryQueuePublisher;
import org.apereo.cas.ticket.registry.queue.commands.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.commands.DeleteTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.commands.DeleteTicketsMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.commands.UpdateTicketMessageQueueCommand;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link AMQPDefaultTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class AMQPDefaultTicketRegistry extends DefaultTicketRegistry implements AMQPTicketRegistry {

    private final TicketRegistryQueuePublisher ticketPublisher;

    private final PublisherIdentifier publisherIdentifier;

    public AMQPDefaultTicketRegistry(final CipherExecutor cipherExecutor,
                                     final TicketSerializationManager ticketSerializationManager,
                                     final TicketCatalog ticketCatalog,
                                     final TicketRegistryQueuePublisher ticketPublisher,
                                     final PublisherIdentifier publisherIdentifier) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.ticketPublisher = ticketPublisher;
        this.publisherIdentifier = publisherIdentifier;
    }

    @Override
    public void addTicketInternal(final @NonNull Ticket ticket) throws Exception {
        addTicketToQueue(ticket);
        LOGGER.trace("Publishing add command for id [{}] and ticket [{}]", publisherIdentifier, ticket.getId());
        val command = new AddTicketMessageQueueCommand(publisherIdentifier, ticket);
        ticketPublisher.publishMessageToQueue(command);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        val result = updateTicketInQueue(ticket);
        LOGGER.trace("Publishing update command for id [{}] and ticket [{}]", publisherIdentifier, ticket.getId());
        val command = new UpdateTicketMessageQueueCommand(publisherIdentifier, ticket);
        ticketPublisher.publishMessageToQueue(command);
        return result;
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        val result = deleteTicketFromQueue(ticketId);
        LOGGER.trace("Publishing delete command for id [{}] and ticket [{}]", publisherIdentifier, ticketId);
        ticketPublisher.publishMessageToQueue(new DeleteTicketMessageQueueCommand(publisherIdentifier, ticketId));
        return result;
    }

    @Override
    public long deleteAll() {
        val result = deleteAllFromQueue();
        ticketPublisher.publishMessageToQueue(new DeleteTicketsMessageQueueCommand(publisherIdentifier));
        return result;
    }


    @Override
    public void addTicketToQueue(final Ticket ticket) throws Exception {
        super.addTicketInternal(ticket);
    }

    @Override
    public Ticket updateTicketInQueue(final Ticket ticket) throws Exception {
        return super.updateTicket(ticket);
    }

    @Override
    public long deleteTicketFromQueue(final String ticketId) {
        return super.deleteSingleTicket(ticketId);
    }

    @Override
    public long deleteAllFromQueue() {
        return super.deleteAll();
    }
}
