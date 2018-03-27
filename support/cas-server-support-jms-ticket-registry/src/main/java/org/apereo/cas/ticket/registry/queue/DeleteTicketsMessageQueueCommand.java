package org.apereo.cas.ticket.registry.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.registry.TicketRegistry;

/**
 * This is {@link DeleteTicketsMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
public class DeleteTicketsMessageQueueCommand extends BaseMessageQueueCommand {


    @JsonCreator
    public DeleteTicketsMessageQueueCommand(@JsonProperty("id") final StringBean id) {
        super(id);

    }

    @Override
    public void execute(final TicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to delete tickets", getId().getId());
        registry.deleteAll();
    }
}
