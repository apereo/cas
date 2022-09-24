package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.ticket.registry.queue.commands.BaseMessageQueueCommand;

/**
 * This is {@link TicketRegistryQueuePublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface TicketRegistryQueuePublisher {
    /**
     * Publish message to queue.
     *
     * @param cmd the cmd
     */
    void publishMessageToQueue(BaseMessageQueueCommand cmd);
}
