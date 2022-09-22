package org.apereo.cas.ticket.registry.queue.commands;

import org.apereo.cas.ticket.registry.AMQPTicketRegistry;
import org.apereo.cas.util.PublisherIdentifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * This is {@link DeleteTicketsMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@ToString(callSuper = true)
public class DeleteTicketsMessageQueueCommand extends BaseMessageQueueCommand {
    @Serial
    private static final long serialVersionUID = 8907022828993467474L;

    @JsonCreator
    public DeleteTicketsMessageQueueCommand(final PublisherIdentifier id) {
        super(id);
    }

    @Override
    public void execute(final AMQPTicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to delete tickets", getId().getId());
        registry.deleteAllFromQueue();
    }

    @Override
    public BaseMessageQueueCommand withId(final PublisherIdentifier id) {
        return new DeleteTicketsMessageQueueCommand(id);
    }
}
