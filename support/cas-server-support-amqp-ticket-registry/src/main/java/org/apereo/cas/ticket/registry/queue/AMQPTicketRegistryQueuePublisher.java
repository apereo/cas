package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AMQPTicketRegistryQueuePublisher implements QueueableTicketRegistryMessagePublisher {
    /**
     * Queue destination name.
     */
    public static final String QUEUE_DESTINATION = "CasTicketRegistryQueue";

    private final RabbitOperations rabbitTemplate;

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
