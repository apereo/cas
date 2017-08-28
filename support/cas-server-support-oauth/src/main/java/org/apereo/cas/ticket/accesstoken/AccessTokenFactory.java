package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.Collection;

/**
 * Factory to create OAuth access tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface AccessTokenFactory extends TicketFactory {

    /**
     * Create an access token.
     *
     * @param service              the service
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @return the access token
     */
    AccessToken create(Service service, Authentication authentication, TicketGrantingTicket ticketGrantingTicket, Collection<String> scopes);
}
