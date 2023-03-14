package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.fi.util.function.CheckedFunction;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This is {@link GoogleCloudTicketRegistryMessageQueueConsumer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class GoogleCloudTicketRegistryMessageQueueConsumer implements Consumer<BasicAcknowledgeablePubsubMessage> {
    private final Topic topic;

    private final Subscription subscription;

    private final QueueableTicketRegistryMessageReceiver messageQueueTicketRegistryReceiver;

    private final PubSubMessageConverter pubSubMessageConverter;

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void accept(final BasicAcknowledgeablePubsubMessage message) {
        FunctionUtils.doAndHandle(o -> {
            val subName = message.getProjectSubscriptionName().getSubscription();
            LOGGER.debug("Message received from [{}] subscription: [{}]", subName,
                message.getPubsubMessage().getData().toStringUtf8());
            val command = pubSubMessageConverter.fromPubSubMessage(message.getPubsubMessage(), BaseMessageQueueCommand.class);
            messageQueueTicketRegistryReceiver.receive(command);
            Objects.requireNonNull(message.ack());
        }, (CheckedFunction<Throwable, Object>) e -> {
            LoggingUtils.error(LOGGER, e);
            return message.nack();
        }).accept(message);
    }
}
