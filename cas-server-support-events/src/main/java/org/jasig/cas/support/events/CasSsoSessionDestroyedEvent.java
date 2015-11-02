package org.jasig.cas.support.events;

import org.jasig.cas.authentication.Authentication;

/**
 * Concrete subclass of {@code AbstractCasSsoEvent} representing single sign on session
 * destruction event e.g. user logged out
 * and <i>TicketGrantingTicket</i> has been destroyed by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class CasSsoSessionDestroyedEvent extends AbstractCasSsoEvent {

    private static final long serialVersionUID = 584961303690286494L;

    /**
     * Instantiates a new Cas sso session destroyed event.
     *
     * @param source                 the source
     * @param authentication         the authentication
     * @param ticketGrantingTicketId the ticket granting ticket id
     */
    public CasSsoSessionDestroyedEvent(final Object source, final Authentication authentication,
                                       final String ticketGrantingTicketId) {
        super(source, authentication, ticketGrantingTicketId);
    }
}
