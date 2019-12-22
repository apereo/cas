package org.apereo.cas.ticket.registry;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.queue.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.DeleteTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.DeleteTicketsMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.UpdateTicketMessageQueueCommand;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link JmsTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JmsTicketRegistry extends DefaultTicketRegistry {

    private final JmsTicketRegistryPublisher ticketPublisher;

    private final JmsQueueIdentifier id;

    public JmsTicketRegistry(final JmsTicketRegistryPublisher publisher, final JmsQueueIdentifier id,
                             final CipherExecutor cipherExecutor) {
        super(cipherExecutor);
        this.ticketPublisher = publisher;
        this.id = id;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        super.addTicket(ticket);
        LOGGER.trace("Publishing add command for id [{}] and ticket [{}]", id, ticket.getId());
        ticketPublisher.publishMessageToQueue(new AddTicketMessageQueueCommand(id, ticket));
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val result = super.deleteSingleTicket(ticketId);
        LOGGER.trace("Publishing delete command for id [{}] and ticket [{}]", id, ticketId);
        ticketPublisher.publishMessageToQueue(new DeleteTicketMessageQueueCommand(id, ticketId));
        return result;
    }

    @Override
    public long deleteAll() {
        val result = super.deleteAll();
        ticketPublisher.publishMessageToQueue(new DeleteTicketsMessageQueueCommand(id));
        return result;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        val result = super.updateTicket(ticket);
        LOGGER.trace("Publishing update command for id [{}] and ticket [{}]", id, ticket.getId());
        ticketPublisher.publishMessageToQueue(new UpdateTicketMessageQueueCommand(id, ticket));
        return result;
    }
}
