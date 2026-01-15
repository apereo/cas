package org.apereo.cas.ticket.registry.publisher;

import module java.base;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.events.KafkaMessagePublishedEvent;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.commands.TicketAwareQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaOperations;

/**
 * This is {@link KafkaTicketRegistryPublisher}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class KafkaTicketRegistryPublisher implements QueueableTicketRegistryMessagePublisher {
    private final ApplicationContext applicationContext;
    private final TicketCatalog ticketCatalog;
    private final KafkaOperations kafkaOperations;

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

    protected void publishMessage(final BaseMessageQueueCommand cmd, final String topic) throws Exception {
        LOGGER.debug("[{}] is publishing message [{}]", cmd.getPublisherIdentifier().getId(), cmd);
        val future = kafkaOperations.send(topic, cmd.getPublisherIdentifier().getId(), cmd);
        Objects.requireNonNull(future);
        future.whenComplete((result, error) -> {
            if (error != null) {
                LOGGER.error("Failed to publish message [{}]: [{}]", cmd, error);
            } else {
                LOGGER.trace("Sent message [{}] from ticket registry id [{}]", cmd, cmd.getPublisherIdentifier());
            }
            applicationContext.publishEvent(new KafkaMessagePublishedEvent(this, topic, cmd));
        }).get();
    }
}
