package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.queue.BaseMessageQueueCommand;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;

/**
 * This is {@link JmsTicketRegistryDefaultPublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class JmsTicketRegistryDefaultPublisher implements JmsTicketRegistryPublisher {
    private final JmsTemplate jmsTemplate;

    @Override
    public void publishMessageToQueue(final BaseMessageQueueCommand cmd) {
        jmsTemplate.convertAndSend(QUEUE_DESTINATION, cmd,
            message -> {
                LOGGER.trace("Sending message [{}] from ticket registry id [{}]", message, cmd.getId());
                return message;
            });
    }
}
