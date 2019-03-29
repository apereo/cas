package org.apereo.cas.support.events.ticket;

import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Getter;
import lombok.ToString;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing single sign on session
 * destruction event e.g. user logged out
 * and <i>TicketGrantingTicket</i> has been destroyed by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@ToString(callSuper = true)
@Getter
public class CasTicketGrantingTicketDestroyedEvent extends AbstractCasTicketGrantingTicketEvent {

    private static final long serialVersionUID = 584961303690286494L;

    /**
     * Instantiates a new CAS sso session destroyed event.
     *
     * @param source the source
     * @param ticket the ticket
     */
    public CasTicketGrantingTicketDestroyedEvent(final Object source, final TicketGrantingTicket ticket) {
        super(source, ticket);
    }
}
