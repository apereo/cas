package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.util.function.FunctionUtils;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collections;
import java.util.Objects;

/**
 * This is {@link GoogleCloudTicketRegistryQueuePublisher}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class GoogleCloudTicketRegistryQueuePublisher implements QueueableTicketRegistryMessagePublisher {
    /**
     * Topic destination name.
     */
    public static final String QUEUE_TOPIC = "CasTicketRegistryTopic";

    /**
     * The dead-letter topic name.
     */
    public static final String DEAD_LETTER_TOPIC = "%sDeadLetter".formatted(QUEUE_TOPIC);

    private final PubSubTemplate pubSubTemplate;

    @Override
    public void publishMessageToQueue(final BaseMessageQueueCommand cmd) {
        FunctionUtils.doAndHandle(__ -> {
            LOGGER.debug("[{}] is publishing message [{}]", cmd.getId().getId(), cmd);
            val headers = Collections.singletonMap(GcpPubSubHeaders.ORDERING_KEY, cmd.getId().getId());
            val future = pubSubTemplate.publish(QUEUE_TOPIC, cmd, headers);
            Objects.requireNonNull(future);
            val publishedMessage = future.get();
            LOGGER.trace("Sent message [{}] from ticket registry id [{}]", publishedMessage, cmd.getId());
        });
    }
}
