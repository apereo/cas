package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.ticket.registry.queue.commands.BaseMessageQueueCommand;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitOperations;

/**
 * This is {@link AMQPTicketRegistryQueuePublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public record AMQPTicketRegistryQueuePublisher(RabbitOperations rabbitTemplate) implements TicketRegistryQueuePublisher {
    /**
     * Queue destination name.
     */
    public static final String QUEUE_DESTINATION = "CasTicketRegistryQueue";

    @Override
    public void publishMessageToQueue(final BaseMessageQueueCommand cmd) {
        LOGGER.debug("[{}] is publishing message [{}]", cmd.getId().getId(), cmd);
        rabbitTemplate.convertAndSend(QUEUE_DESTINATION, StringUtils.EMPTY, cmd,
            message -> {
                LOGGER.trace("Sent message [{}] from ticket registry id [{}]", message, cmd.getId());
                return message;
            });
    }
}
