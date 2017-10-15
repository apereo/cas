package org.apereo.cas.ticket.registry.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link DeleteTicketsMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class DeleteTicketsMessageQueueCommand extends BaseMessageQueueCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteTicketsMessageQueueCommand.class);

    @JsonCreator
    public DeleteTicketsMessageQueueCommand(@JsonProperty("id") final StringBean id) {
        super(id);

    }

    @Override
    public void execute(final TicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to delete tickets");
        registry.deleteAll();
    }
}
