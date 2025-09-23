package org.apereo.cas.support.events.logout;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;
import java.io.Serial;

/**
 * Concrete subclass of {@link AbstractCasEvent} representing a request for SLO.
 *
 * @author Jerome LELEU
 * @since 7.2
 */
@ToString(callSuper = true)
@Getter
public class CasRequestSingleLogoutEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -186425713441224237L;
    
    private final TicketGrantingTicket ticketGrantingTicket;

    public CasRequestSingleLogoutEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket,
                                       final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.ticketGrantingTicket = ticketGrantingTicket;
    }
}
