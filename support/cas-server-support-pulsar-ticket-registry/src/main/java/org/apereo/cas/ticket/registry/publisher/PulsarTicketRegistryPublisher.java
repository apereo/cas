package org.apereo.cas.ticket.registry.publisher;

import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.commands.TicketAwareQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.pulsar.core.PulsarTemplate;

/**
 * This is {@link PulsarTicketRegistryPublisher}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class PulsarTicketRegistryPublisher implements QueueableTicketRegistryMessagePublisher {
    private final TicketCatalog ticketCatalog;
    private final PulsarTemplate pulsarTemplate;

    @Override
    public void publishMessageToQueue(final BaseMessageQueueCommand cmd) {
        FunctionUtils.doAndHandle(_ -> {
            if (cmd instanceof final TicketAwareQueueCommand taqm) {
                val topic = ticketCatalog.find(taqm.getTicketId()).getProperties().getStorageName();
                publishMessage(cmd, topic);
            } else {
                for (val ticketDefinition : ticketCatalog.findAll()) {
                    val topic = ticketDefinition.getProperties().getStorageName();
                    publishMessage(cmd, topic);
                }
            }
        });
    }

    private void publishMessage(final BaseMessageQueueCommand cmd, final String topic) {
        LOGGER.debug("[{}] is publishing message [{}]", cmd.getPublisherIdentifier().getId(), cmd);
        val id = pulsarTemplate.send(topic, cmd);
        LOGGER.debug("Sent message with id [{}] [{}] from ticket registry id [{}]", id, cmd, cmd.getPublisherIdentifier());
    }
}
