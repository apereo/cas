package org.apereo.cas.support.events.logout;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * Concrete subclass of {@link AbstractCasEvent} representing a request for SLO.
 *
 * @author Jerome LELEU
 * @since 7.2
 */
@ToString(callSuper = true)
@Getter
public class CasRequestSingleLogoutEvent extends AbstractCasEvent {

    private final TicketGrantingTicket ticketGrantingTicket;

    /**
     * Instantiates a new CAS request single logout event.
     *
     * @param source               the source
     * @param ticketGrantingTicket the ticket granting ticket
     * @param clientInfo           the client info
     */
    public CasRequestSingleLogoutEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket,
                                       final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.ticketGrantingTicket = ticketGrantingTicket;
    }
}
