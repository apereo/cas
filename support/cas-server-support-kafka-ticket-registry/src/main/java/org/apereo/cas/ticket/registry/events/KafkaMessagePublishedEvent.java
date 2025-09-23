package org.apereo.cas.ticket.registry.events;

import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import java.io.Serial;

/**
 * This is {@link KafkaMessagePublishedEvent}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@ToString
public class KafkaMessagePublishedEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = -4316096469433022995L;

    private final BaseMessageQueueCommand command;
    private final String topic;

    public KafkaMessagePublishedEvent(final Object source, final String topic,
                                      final BaseMessageQueueCommand command) {
        super(source);
        this.topic = topic;
        this.command = command;
    }
}
