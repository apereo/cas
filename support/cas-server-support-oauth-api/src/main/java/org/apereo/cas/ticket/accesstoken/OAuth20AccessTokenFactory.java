package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.Collection;
import java.util.HashMap;
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
     * @param token                the token
     * @param clientId             the client id
     * @param requestClaims        the request claims
     * @param responseType         the response type
     * @param grantType            the grant type
     * @return the access token
     */
    OAuth20AccessToken create(Service service,
                              Authentication authentication,
                              TicketGrantingTicket ticketGrantingTicket,
                              Collection<String> scopes,
                              String token,
                              String clientId,
                              Map<String, Map<String, Object>> requestClaims,
                              OAuth20ResponseTypes responseType,
                              OAuth20GrantTypes grantType);

    /**
     * Create access token.
     *
     * @param service        the service
     * @param authentication the authentication
     * @param scopes         the scopes
     * @param clientId       the client id
     * @param responseType   the response type
     * @param grantType      the grant type
     * @return the access token
     */
    default OAuth20AccessToken create(final Service service,
                                      final Authentication authentication,
                                      final Collection<String> scopes,
                                      final String clientId,
                                      final OAuth20ResponseTypes responseType,
                                      final OAuth20GrantTypes grantType) {
        return create(service, authentication, null, scopes, null, clientId,
            new HashMap<>(), responseType, grantType);
    }
}
