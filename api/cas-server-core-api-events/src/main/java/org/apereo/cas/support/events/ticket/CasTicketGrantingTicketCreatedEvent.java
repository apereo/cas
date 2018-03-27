package org.apereo.cas.support.events.ticket;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.ToString;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing single sign on session establishment
 * event e.g. user logged in
 * and <i>TicketGrantingTicket</i> has been vended by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Slf4j
@ToString
public class CasTicketGrantingTicketCreatedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -1862937393590213844L;

    private final TicketGrantingTicket ticketGrantingTicket;

    /**
     * Instantiates a new Cas sso session established event.
     *
     * @param source               the source
     * @param ticketGrantingTicket the ticket granting ticket
     */
    public CasTicketGrantingTicketCreatedEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket) {
        super(source);
        this.ticketGrantingTicket = ticketGrantingTicket;
    }

    public TicketGrantingTicket getTicketGrantingTicket() {
        return this.ticketGrantingTicket;
    }
}
