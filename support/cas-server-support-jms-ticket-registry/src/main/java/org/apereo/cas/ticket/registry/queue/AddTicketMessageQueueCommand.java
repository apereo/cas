package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link AddTicketMessageQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@Getter
public class AddTicketMessageQueueCommand extends BaseMessageQueueCommand {
    private static final long serialVersionUID = -7698722632898271240L;

    @JsonProperty
    private Ticket ticket;

    @JsonCreator
    public AddTicketMessageQueueCommand(@JsonProperty("id") final JmsQueueIdentifier id, @JsonProperty("ticket") final Ticket ticket) {
        super(id);
        this.ticket = ticket;
    }

    @Override
    public void execute(final TicketRegistry registry) {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to add ticket [{}]", getId().getId(), ticket);
        registry.addTicket(ticket);
    }
}
