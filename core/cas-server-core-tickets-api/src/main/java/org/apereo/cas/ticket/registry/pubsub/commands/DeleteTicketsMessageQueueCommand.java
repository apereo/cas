package org.apereo.cas.ticket.registry.pubsub.commands;

import org.apereo.cas.ticket.registry.pubsub.QueueableTicketRegistry;
import org.apereo.cas.util.PublisherIdentifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class DeleteTicketsMessageQueueCommand extends BaseMessageQueueCommand {
    @Serial
    private static final long serialVersionUID = 8907022828993467474L;

    @JsonCreator
    public DeleteTicketsMessageQueueCommand(@JsonProperty("id")
                                            final PublisherIdentifier id) {
        super(id);
    }

    @Override
    public void execute(final QueueableTicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to delete tickets", getPublisherIdentifier().getId());
        registry.deleteAllFromQueue();
    }

    @Override
    public BaseMessageQueueCommand withPublisherIdentifier(final PublisherIdentifier id) {
        return new DeleteTicketsMessageQueueCommand(id);
    }
}
