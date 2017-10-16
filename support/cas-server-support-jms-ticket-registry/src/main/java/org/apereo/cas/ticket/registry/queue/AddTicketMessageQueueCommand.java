package org.apereo.cas.ticket.registry.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link AddTicketMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class AddTicketMessageQueueCommand extends BaseMessageQueueCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddTicketMessageQueueCommand.class);

    @JsonProperty
    private Ticket ticket;

    @JsonCreator
    public AddTicketMessageQueueCommand(@JsonProperty("id") final StringBean id, @JsonProperty("ticket") final Ticket ticket) {
        super(id);
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return ticket;
    }

    @Override
    public void execute(final TicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to add ticket [{}]", ticket);
        registry.addTicket(ticket);
    }
}
