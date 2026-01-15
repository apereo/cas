package org.apereo.cas.ticket.registry.pubsub.queue;

import module java.base;
import org.apereo.cas.ticket.registry.pubsub.QueueableTicketRegistry;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.util.PublisherIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link BaseQueueableTicketRegistryMessageReceiver}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class BaseQueueableTicketRegistryMessageReceiver implements QueueableTicketRegistryMessageReceiver {
    private final QueueableTicketRegistry ticketRegistry;

    private final PublisherIdentifier ticketRegistryId;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void receive(final BaseMessageQueueCommand command) throws Exception {
        LOGGER.debug("[{}] received message [{}]", ticketRegistryId, command);
        if (command.getPublisherIdentifier().equals(this.ticketRegistryId)) {
            LOGGER.trace("Ignoring inbound command on ticket registry with id [{}]", this.ticketRegistryId);
        } else {
            LOGGER.debug("Accepting message from ticket registry id [{}]. Executing command [{}]", command.getPublisherIdentifier(), command);
            command.withPublisherIdentifier(this.ticketRegistryId).execute(this.ticketRegistry);
        }
        applicationContext.publishEvent(new QueueableTicketRegistryMessageReceivedEvent(this, command));
    }
}
