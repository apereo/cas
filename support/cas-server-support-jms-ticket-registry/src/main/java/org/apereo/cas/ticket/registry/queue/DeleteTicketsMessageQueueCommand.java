package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.ticket.registry.TicketRegistry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link DeleteTicketsMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
public class DeleteTicketsMessageQueueCommand extends BaseMessageQueueCommand {
    private static final long serialVersionUID = 8907022828993467474L;

    @JsonCreator
    public DeleteTicketsMessageQueueCommand(@JsonProperty("id") final JmsQueueIdentifier id) {
        super(id);

    }

    @Override
    public void execute(final TicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to delete tickets", getId().getId());
        registry.deleteAll();
    }
}
