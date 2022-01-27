package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.queue.BaseMessageQueueCommand;
import org.apereo.cas.ticket.queue.TicketRegistryQueuePublisher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;

/**
 * This is {@link JmsTicketRegistryQueuePublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class JmsTicketRegistryQueuePublisher implements TicketRegistryQueuePublisher {
    /**
     * Queue destination name.
     */
    public static final String QUEUE_DESTINATION = "CasTicketRegistryQueue";

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
