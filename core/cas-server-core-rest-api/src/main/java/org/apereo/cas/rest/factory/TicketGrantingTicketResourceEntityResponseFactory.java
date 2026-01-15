package org.apereo.cas.rest.factory;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link TicketGrantingTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface TicketGrantingTicketResourceEntityResponseFactory {

    /**
     * Build response entity.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param request              the request
     * @return the response entity
     * @throws Throwable the throwable
     */
    ResponseEntity<String> build(Ticket ticketGrantingTicket, HttpServletRequest request) throws Throwable;
}
