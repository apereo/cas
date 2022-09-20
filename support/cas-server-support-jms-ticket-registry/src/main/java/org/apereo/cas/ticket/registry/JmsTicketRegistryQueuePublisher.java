package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.queue.BaseMessageQueueCommand;
import org.apereo.cas.ticket.queue.TicketRegistryQueuePublisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitOperations;

/**
 * This is {@link JmsTicketRegistryQueuePublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public record JmsTicketRegistryQueuePublisher(RabbitOperations jmsTemplate) implements TicketRegistryQueuePublisher {
    /**
     * Queue destination name.
     */
    public static final String QUEUE_DESTINATION = "CasTicketRegistryQueue";

    @Override
    public void publishMessageToQueue(final BaseMessageQueueCommand cmd) {
        jmsTemplate.convertAndSend(QUEUE_DESTINATION, "cas." + cmd.getId().getId(), cmd,
            message -> {
                LOGGER.trace("Sending message [{}] from ticket registry id [{}]", message, cmd.getId());
                return message;
            });
    }
}
