package org.apereo.cas.support.events.ticket;

import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;


/**
 * Concrete subclass of {@code AbstractCasEvent} representing single sign on session establishment
 * event e.g. user logged in
 * and <i>TicketGrantingTicket</i> has been vended by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@ToString(callSuper = true)
public class CasTicketGrantingTicketCreatedEvent extends AbstractCasTicketGrantingTicketEvent {

    @Serial
    private static final long serialVersionUID = -1862937393590213844L;

    /**
     * Instantiates a new CAS sso session established event.
     *
     * @param source               the source
     * @param ticketGrantingTicket the ticket granting ticket
     */
    public CasTicketGrantingTicketCreatedEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket, final ClientInfo clientInfo) {
        super(source, ticketGrantingTicket, clientInfo);
    }
}
