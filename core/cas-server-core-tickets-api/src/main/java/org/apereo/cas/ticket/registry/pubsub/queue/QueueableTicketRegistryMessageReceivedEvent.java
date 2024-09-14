package org.apereo.cas.ticket.registry.pubsub.queue;

import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import java.io.Serial;

/**
 * This is {@link QueueableTicketRegistryMessageReceivedEvent}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@ToString
public class QueueableTicketRegistryMessageReceivedEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = -4316096469433022995L;

    private final BaseMessageQueueCommand command;

    public QueueableTicketRegistryMessageReceivedEvent(final Object source, final BaseMessageQueueCommand command) {
        super(source);
        this.command = command;
    }
}
