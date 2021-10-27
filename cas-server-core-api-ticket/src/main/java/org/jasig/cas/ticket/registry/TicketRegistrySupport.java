package org.jasig.cas.ticket.registry;


import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;

import java.util.Map;

/**
 * Helper strategy API to ease retrieving CAS' <code>Authentication</code> object and its associated components
 * from available CAS SSO String token called <i>Ticket Granting Ticket (TGT)</i>.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public interface TicketRegistrySupport {

    /**
     * Retrieve a valid Authentication object identified by the provided TGT SSO token.
     * @param ticketGrantingTicketId an SSO token identifying the requested Authentication
     * @return valid Authentication OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Authentication getAuthenticationFrom(String ticketGrantingTicketId);

    /**
     * Retrieve a valid Principal object identified by the provided TGT SSO token.
     * @param ticketGrantingTicketId an SSO token identifying the requested authenticated Principal
     * @return valid Principal OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Principal getAuthenticatedPrincipalFrom(String ticketGrantingTicketId);

    /**
     * Retrieve a valid Principal's map of attributes identified by the provided TGT SSO token.
     * @param ticketGrantingTicketId an SSO token identifying the requested authenticated Principal's attributes
     * @return valid Principal's attributes OR <b>NULL</b> if there is no valid SSO session
     * present identified by the provided TGT id SSO token
     */
    Map<String, Object> getPrincipalAttributesFrom(String ticketGrantingTicketId);
}
