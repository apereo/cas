package org.apereo.cas.ticket.queue;

/**
 * This is {@link TicketRegistryQueuePublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface TicketRegistryQueuePublisher {
    /**
     * No Op ticket registry publisher.
     *
     * @return the ticket registry publisher
     */
    static TicketRegistryQueuePublisher noOp() {
        return cmd -> {
        };
    }

    /**
     * Publish message to queue.
     *
     * @param cmd the cmd
     */
    void publishMessageToQueue(BaseMessageQueueCommand cmd);
}
