package org.apereo.cas.token;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * This is {@link TokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface TokenTicketBuilder {
    /**
     * Build token for a service ticket.
     *
     * @param serviceTicketId the ticket id
     * @param service         the service
     * @return the token identifier
     */
    String build(String serviceTicketId, Service service);

    /**
     * Build token for a ticket-granting ticket.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the string
     */
    String build(TicketGrantingTicket ticketGrantingTicket);
}
