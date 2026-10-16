package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ticket.registry.publisher.PulsarTicketRegistryPublisher;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Receives ticket registry commands from Pulsar topics, where they travel as JSON strings,
 * and hands them to the ticket registry message receiver of this node.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class PulsarTicketRegistryMessageListener {
    private final QueueableTicketRegistryMessageReceiver receiver;

    /**
     * Read the command from its JSON representation and execute it against this node's registry.
     *
     * @param message the command as written by {@link PulsarTicketRegistryPublisher}
     * @throws Exception the exception
     */
    public void receive(final String message) throws Exception {
        val command = PulsarTicketRegistryPublisher.MAPPER.readValue(message, BaseMessageQueueCommand.class);
        LOGGER.trace("Received ticket registry command [{}]", command);
        receiver.receive(command);
    }
}
