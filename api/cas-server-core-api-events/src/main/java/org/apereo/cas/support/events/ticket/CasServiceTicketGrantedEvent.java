package org.apereo.cas.support.events.ticket;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Getter;
import lombok.ToString;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing granting of a
 * service ticket by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@ToString(callSuper = true)
@Getter
public class CasServiceTicketGrantedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 128616377249711105L;

    private final TicketGrantingTicket ticketGrantingTicket;

    private final ServiceTicket serviceTicket;

    /**
     * Instantiates a new CAS service ticket granted event.
     *
     * @param source               the source
     * @param ticketGrantingTicket the ticket granting ticket
     * @param serviceTicket        the service ticket
     */
    public CasServiceTicketGrantedEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket, final ServiceTicket serviceTicket) {
        super(source);
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.serviceTicket = serviceTicket;
    }
}
