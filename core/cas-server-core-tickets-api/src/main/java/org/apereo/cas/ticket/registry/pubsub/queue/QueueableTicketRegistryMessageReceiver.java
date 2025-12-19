package org.apereo.cas.ticket.registry.pubsub.queue;

import module java.base;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;

/**
 * This is {@link QueueableTicketRegistryMessageReceiver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface QueueableTicketRegistryMessageReceiver {

    /**
     * No op queueable ticket registry message receiver that does nothing.
     *
     * @return the queueable ticket registry message receiver
     */
    static QueueableTicketRegistryMessageReceiver noOp() {
        return command -> {
        };
    }
    /**
     * Receive message from queue and execute command.
     *
     * @param command the command
     * @throws Exception the exception
     */
    void receive(BaseMessageQueueCommand command) throws Exception;
}
