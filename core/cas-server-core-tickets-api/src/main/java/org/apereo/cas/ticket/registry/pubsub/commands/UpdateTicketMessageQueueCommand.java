package org.apereo.cas.ticket.registry.pubsub.commands;

import org.apereo.cas.ticket.Ticket;
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
 * This is {@link UpdateTicketMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateTicketMessageQueueCommand extends BaseMessageQueueCommand {
    @Serial
    private static final long serialVersionUID = -4179190682337040669L;

    private final Ticket ticket;

    @JsonCreator
    public UpdateTicketMessageQueueCommand(
        @JsonProperty("id")
        final PublisherIdentifier id,
        @JsonProperty("ticket")
        final Ticket ticket) {
        super(id);
        this.ticket = ticket;
    }

    @Override
    public void execute(final QueueableTicketRegistry registry) throws Exception {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to update ticket [{}]", getId().getId(), ticket);
        registry.updateTicketInQueue(this.ticket);
    }

    @Override
    public BaseMessageQueueCommand withId(final PublisherIdentifier id) {
        return new UpdateTicketMessageQueueCommand(id, this.ticket);
    }
}
