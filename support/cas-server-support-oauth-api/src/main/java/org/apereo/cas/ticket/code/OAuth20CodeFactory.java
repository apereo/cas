package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.Collection;
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
     * @param requestClaims               the claims
     * @return the OAuth code
     */
    OAuth20Code create(Service service,
                       Authentication authentication,
                       TicketGrantingTicket ticketGrantingTicket,
                       Collection<String> scopes,
                       String codeChallenge,
                       String codeChallengeMethod,
                       String clientId,
                       Map<String, Map<String, Object>> requestClaims);
}
