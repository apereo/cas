package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create OAuth codes.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20CodeFactory extends TicketFactory {

    /**
     * Create an OAuth code.
     *
     * @param service              the service
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @param codeChallenge        the code challenge
     * @param codeChallengeMethod  the code challenge method
     * @param clientId             the client id
     * @param requestClaims        the claims
     * @param responseType         the response type
     * @param grantType            the grant type
     * @return the OAuth code
     * @throws Throwable the throwable
     */
    OAuth20Code create(Service service,
                       Authentication authentication,
                       Ticket ticketGrantingTicket,
                       Collection<String> scopes,
                       String codeChallenge,
                       String codeChallengeMethod,
                       String clientId,
                       Map<String, Map<String, Object>> requestClaims,
                       OAuth20ResponseTypes responseType,
                       OAuth20GrantTypes grantType) throws Throwable;

    /**
     * Create OAuth code without code challenge or method.
     *
     * @param service              the service
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @param clientId             the client id
     * @param responseType         the response type
     * @param grantType            the grant type
     * @return the oauth code
     * @throws Throwable the throwable
     */
    default OAuth20Code create(final Service service,
                               final Authentication authentication,
                               final Ticket ticketGrantingTicket,
                               final Collection<String> scopes,
                               final String clientId,
                               final OAuth20ResponseTypes responseType,
                               final OAuth20GrantTypes grantType) throws Throwable {
        return create(service, authentication, ticketGrantingTicket, scopes,
            null, null, clientId, new HashMap<>(), responseType, grantType);
    }
}
