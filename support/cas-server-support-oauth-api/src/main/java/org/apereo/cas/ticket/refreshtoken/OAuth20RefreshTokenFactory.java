package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Factory to create OAuth refresh tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20RefreshTokenFactory extends TicketFactory {

    /**
     * Create a refresh token.
     *
     * @param service              the service
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @param clientId             the client id
     * @param accessToken          the access token created with this refresh token
     * @param requestClaims        the request claims
     * @param responseType         the response type
     * @param grantType            the grant type
     * @return the refresh token
     * @throws Throwable the throwable
     */
    OAuth20RefreshToken create(Service service, Authentication authentication,
                               Ticket ticketGrantingTicket,
                               Collection<String> scopes,
                               String clientId,
                               String accessToken,
                               Map<String, Map<String, Object>> requestClaims,
                               OAuth20ResponseTypes responseType,
                               OAuth20GrantTypes grantType) throws Throwable;
}
