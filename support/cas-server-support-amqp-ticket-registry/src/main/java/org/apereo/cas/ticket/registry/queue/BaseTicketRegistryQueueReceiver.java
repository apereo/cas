package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.ticket.registry.AMQPTicketRegistry;
import org.apereo.cas.ticket.registry.queue.commands.BaseMessageQueueCommand;
import org.apereo.cas.util.PublisherIdentifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link BaseTicketRegistryQueueReceiver}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class BaseTicketRegistryQueueReceiver {
    private final AMQPTicketRegistry ticketRegistry;

    private final PublisherIdentifier ticketRegistryId;

    /**
     * Receive message from queue and execute command.
     *
     * @param command the command
     * @throws Exception the exception
     */
    public void receive(final BaseMessageQueueCommand command) throws Exception {
        LOGGER.debug("[{}] received message [{}]", ticketRegistryId, command);
        if (command.getId().equals(this.ticketRegistryId)) {
            LOGGER.trace("Ignoring inbound command on ticket registry with id [{}]", this.ticketRegistryId);
        } else {
            LOGGER.debug("Accepting message from ticket registry id [{}]. Executing command [{}]", command.getId(), command);
            command.withId(this.ticketRegistryId).execute(this.ticketRegistry);
        }
    }
}
