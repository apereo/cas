package org.jasig.cas.support.oauth.web;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.Pair;

/**
 * This is the contract {@link AccessTokenGenerator}
 * that specified how access tokens should be generated and parsed from the request.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AccessTokenGenerator {

    /**
     * Generate an access token.
     *
     * @param service              the service
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the token
     */
    String generate(Service service, TicketGrantingTicket ticketGrantingTicket);

    /**
     * Degenerate an access token received from the client and
     * translates it to something CAS can understand as an access token.
     *
     * @param accessTokenInput the access token input
     * @return the parsed token
     */
    Pair<String, Service> degenerate(String accessTokenInput);
}
