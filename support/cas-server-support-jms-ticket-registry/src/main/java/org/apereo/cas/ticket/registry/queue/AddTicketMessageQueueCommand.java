package org.apereo.cas.ticket.registry.queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;

/**
 * This is {@link AddTicketMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
public class AddTicketMessageQueueCommand extends BaseMessageQueueCommand {


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
        LOGGER.debug("Executing queue command on ticket registry id [{}] to add ticket [{}]", getId().getId(), ticket);
        registry.addTicket(ticket);
    }
}
