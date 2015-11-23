package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing granting of a
 * service ticket by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class CasServiceTicketGrantedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 128616377249711105L;

    private final TicketGrantingTicket ticketGrantingTicket;
    private final ServiceTicket serviceTicket;

    /**
     * Instantiates a new Cas service ticket granted event.
     *
     * @param source               the source
     * @param ticketGrantingTicket the ticket granting ticket
     * @param serviceTicket        the service ticket
     */
    public CasServiceTicketGrantedEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket,
                                        final ServiceTicket serviceTicket) {
        super(source);
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.serviceTicket = serviceTicket;
    }

    public TicketGrantingTicket getTicketGrantingTicket() {
        return ticketGrantingTicket;
    }

    public ServiceTicket getServiceTicket() {
        return serviceTicket;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ticketGrantingTicket", ticketGrantingTicket)
                .append("serviceTicket", serviceTicket)
                .toString();
    }
}
