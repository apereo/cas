package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.Collection;
import java.util.Map;

/**
 * Factory to create OAuth access tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20AccessTokenFactory extends TicketFactory {

    /**
     * Create an access token.
     *
     * @param service              the service
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @param clientId             the client id
     * @param requestClaims        the request claims
     * @return the access token
     */
    OAuth20AccessToken create(Service service, Authentication authentication,
                              TicketGrantingTicket ticketGrantingTicket,
                              Collection<String> scopes,
                              String clientId,
                              Map<String, Map<String, Object>> requestClaims);

    /**
     * Create access token.
     *
     * @param service        the service
     * @param authentication the authentication
     * @param scopes         the scopes
     * @param requestClaims  the request claims
     * @param clientId       the client id
     * @return the access token
     */
    OAuth20AccessToken create(Service service, Authentication authentication,
                              Collection<String> scopes,
                              String clientId,
                              Map<String, Map<String, Object>> requestClaims);
}
