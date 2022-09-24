package org.apereo.cas.ticket.registry.queue.commands;

import org.apereo.cas.ticket.registry.AMQPTicketRegistry;
import org.apereo.cas.util.PublisherIdentifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * This is {@link DeleteTicketMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@Getter
@ToString(callSuper = true)
public class DeleteTicketMessageQueueCommand extends BaseMessageQueueCommand {

    @Serial
    private static final long serialVersionUID = 8183330712274484245L;

    private final String ticketId;

    @JsonCreator
    public DeleteTicketMessageQueueCommand(final PublisherIdentifier id,
                                           final String ticketId) {
        super(id);
        this.ticketId = ticketId;
    }

    @Override
    public void execute(final AMQPTicketRegistry registry) throws Exception {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to delete ticket [{}]", getId().getId(), ticketId);
        registry.deleteTicketFromQueue(this.ticketId);
    }

    @Override
    public BaseMessageQueueCommand withId(final PublisherIdentifier id) {
        return new DeleteTicketMessageQueueCommand(id, this.ticketId);
    }
}
