package org.apereo.cas.ticket.queue;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.PublisherIdentifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;

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
    @Serial
    private static final long serialVersionUID = -7698722632898271240L;

    @JsonProperty
    private String ticket;

    @JsonProperty
    private String ticketType;

    @JsonCreator
    public AddTicketMessageQueueCommand(
        @JsonProperty("id")
        final PublisherIdentifier id,
        @JsonProperty("ticket")
        final String ticket,
        @JsonProperty("ticketType")
        final String ticketType) {
        super(id);
        this.ticket = ticket;
        this.ticketType = ticketType;
    }

    @Override
    public void execute(final TicketRegistry registry) throws Exception {
        LOGGER.debug("Executing queue command on ticket registry id [{}] to add ticket [{}]", getId().getId(), ticket);
        val result = deserializeTicket(ticket, ticketType);
        registry.addTicket(result);
    }
}
