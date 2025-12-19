package org.apereo.cas.ticket.registry.pubsub.queue;

import module java.base;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;

/**
 * This is {@link QueueableTicketRegistryMessagePublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface QueueableTicketRegistryMessagePublisher {

    /**
     * No op ticket registry message publisher
     * that does nothing and disables the publisher.
     *
     * @return the queueable ticket registry message publisher
     */
    static QueueableTicketRegistryMessagePublisher noOp() {
        return new QueueableTicketRegistryMessagePublisher() {
            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }

    /**
     * Publish message to queue.
     * Default implementation is do nothing.
     * @param cmd the cmd
     */
    default void publishMessageToQueue(final BaseMessageQueueCommand cmd) {
    }

    /**
     * Is publishing enabled?
     *
     * @return true/false
     */
    default boolean isEnabled() {
        return true;
    }
}
