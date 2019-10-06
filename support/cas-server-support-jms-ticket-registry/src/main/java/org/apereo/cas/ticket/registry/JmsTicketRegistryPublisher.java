package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.queue.BaseMessageQueueCommand;

/**
 * This is {@link JmsTicketRegistryPublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface JmsTicketRegistryPublisher {
    /**
     * Queue destination name.
     */
    String QUEUE_DESTINATION = "CasJmsTicketRegistry";

    /**
     * No Op jms ticket registry publisher.
     *
     * @return the jms ticket registry publisher
     */
    static JmsTicketRegistryPublisher noOp() {
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
