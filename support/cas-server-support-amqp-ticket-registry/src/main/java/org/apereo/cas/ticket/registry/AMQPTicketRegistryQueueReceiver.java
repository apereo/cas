package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.queue.BaseTicketRegistryQueueReceiver;
import org.apereo.cas.util.PublisherIdentifier;

/**
 * This is {@link AMQPTicketRegistryQueueReceiver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class AMQPTicketRegistryQueueReceiver extends BaseTicketRegistryQueueReceiver {
    public AMQPTicketRegistryQueueReceiver(final AMQPTicketRegistry ticketRegistry, final PublisherIdentifier ticketRegistryId) {
        super(ticketRegistry, ticketRegistryId);
    }
}
