package org.apereo.cas.ticket;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TicketGrantingTicketAwareTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TicketGrantingTicketAwareTicket extends AuthenticationAwareTicket {
    /**
     * Method to retrieve the {@link TicketGrantingTicket} that granted this ticket.
     *
     * @return the ticket or null if it has no parent
     */
    default @Nullable Ticket getTicketGrantingTicket() {
        return null;
    }
}
