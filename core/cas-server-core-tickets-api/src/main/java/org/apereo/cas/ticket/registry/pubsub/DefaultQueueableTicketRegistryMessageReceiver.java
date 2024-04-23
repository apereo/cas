package org.apereo.cas.ticket.registry.pubsub;

import org.apereo.cas.ticket.registry.pubsub.queue.BaseQueueableTicketRegistryMessageReceiver;
import org.apereo.cas.util.PublisherIdentifier;

/**
 * This is {@link DefaultQueueableTicketRegistryMessageReceiver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultQueueableTicketRegistryMessageReceiver extends BaseQueueableTicketRegistryMessageReceiver {
    public DefaultQueueableTicketRegistryMessageReceiver(final QueueableTicketRegistry ticketRegistry, final PublisherIdentifier ticketRegistryId) {
        super(ticketRegistry, ticketRegistryId);
    }
}
