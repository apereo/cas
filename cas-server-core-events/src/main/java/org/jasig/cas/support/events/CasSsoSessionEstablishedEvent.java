package org.jasig.cas.support.events;

import org.jasig.cas.authentication.Authentication;

/**
 * Concrete subclass of {@code AbstractCasSsoEvent} representing single sign on session establishment
 * event e.g. user logged in
 * and <i>TicketGrantingTicket</i> has been vended by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class CasSsoSessionEstablishedEvent extends AbstractCasSsoEvent {

    private static final long serialVersionUID = -1862937393590213844L;

    /**
     * Instantiates a new Cas sso session established event.
     *
     * @param source                 the source
     * @param authentication         the authentication
     * @param ticketGrantingTicketId the ticket granting ticket id
     */
    public CasSsoSessionEstablishedEvent(final Object source,
                                         final Authentication authentication,
                                         final String ticketGrantingTicketId) {
        super(source, authentication, ticketGrantingTicketId);
    }
}
