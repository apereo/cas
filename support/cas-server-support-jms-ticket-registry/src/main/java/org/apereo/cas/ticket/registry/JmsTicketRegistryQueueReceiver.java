package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.queue.BaseMessageQueueCommand;
import org.apereo.cas.ticket.queue.BaseTicketRegistryQueueReceiver;
import org.apereo.cas.util.PublisherIdentifier;

import org.springframework.jms.annotation.JmsListener;

/**
 * This is {@link JmsTicketRegistryQueueReceiver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JmsTicketRegistryQueueReceiver extends BaseTicketRegistryQueueReceiver {
    public JmsTicketRegistryQueueReceiver(final TicketRegistry ticketRegistry, final PublisherIdentifier ticketRegistryId) {
        super(ticketRegistry, ticketRegistryId);
    }

    @JmsListener(destination = JmsTicketRegistryQueuePublisher.QUEUE_DESTINATION, containerFactory = "messageQueueTicketRegistryFactory")
    @Override
    public void receive(final BaseMessageQueueCommand command) throws Exception {
        super.receive(command);
    }
}
