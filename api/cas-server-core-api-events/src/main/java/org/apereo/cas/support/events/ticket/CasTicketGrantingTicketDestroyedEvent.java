package org.apereo.cas.support.events.ticket;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing single sign on session
 * destruction event e.g. user logged out
 * and <i>TicketGrantingTicket</i> has been destroyed by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public class CasTicketGrantingTicketDestroyedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 584961303690286494L;

    private final TicketGrantingTicket ticketGrantingTicket;

    /**
     * Instantiates a new Cas sso session destroyed event.
     *
     * @param source the source
     * @param ticket the ticket
     */
    public CasTicketGrantingTicketDestroyedEvent(final Object source, final TicketGrantingTicket ticket) {
        super(source);
        this.ticketGrantingTicket = ticket;
    }

    public TicketGrantingTicket getTicketGrantingTicket() {
        return this.ticketGrantingTicket;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ticketGrantingTicket", this.ticketGrantingTicket)
                .toString();
    }
}
