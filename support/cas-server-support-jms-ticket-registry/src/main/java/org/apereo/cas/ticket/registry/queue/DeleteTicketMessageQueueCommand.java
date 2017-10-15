package org.apereo.cas.ticket.registry.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link DeleteTicketMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class DeleteTicketMessageQueueCommand extends BaseMessageQueueCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteTicketMessageQueueCommand.class);
    
    @JsonProperty
    private String ticketId;

    @JsonCreator
    public DeleteTicketMessageQueueCommand(@JsonProperty("id") final StringBean id, @JsonProperty("ticketId") final String ticketId) {
        super(id);
        this.ticketId = ticketId;
    }

    public String getTicketId() {
        return ticketId;
    }

    @Override
    public void execute(final TicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to delete ticket [{}]", ticketId);
        registry.deleteTicket(this.ticketId);
    }
}
