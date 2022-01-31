package org.apereo.cas.ticket.queue;

import org.apereo.cas.ticket.registry.TicketRegistry;
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
    private final TicketRegistry ticketRegistry;

    private final PublisherIdentifier ticketRegistryId;

    /**
     * Receive message from queue and execute command.
     *
     * @param command the command
     * @throws Exception the exception
     */
    public void receive(final BaseMessageQueueCommand command) throws Exception {
        if (!command.getId().equals(getTicketRegistryId())) {
            LOGGER.debug("Received message from ticket registry id [{}]. Executing command [{}]",
                command.getId(), command.getClass().getSimpleName());
            command.execute(getTicketRegistry());
        } else {
            LOGGER.trace("Ignoring inbound command on ticket registry with id [{}]", getTicketRegistryId());
        }
    }
}
