package org.apereo.cas.ticket.registry;


import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;

import java.util.Map;

/**
 * Helper strategy API to ease retrieving CAS' {@code Authentication} object and its associated components
 * from available CAS SSO String token called <i>Ticket Granting Ticket (TGT)</i>.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public interface TicketRegistrySupport {

    /**
     * Retrieve a valid Authentication object identified by the provided TGT SSO token.
     *
     * @param ticketGrantingTicketId an SSO token identifying the requested Authentication
     * @return valid Authentication OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Authentication getAuthenticationFrom(String ticketGrantingTicketId);

    /**
     * Retrieve a valid Principal object identified by the provided TGT SSO token.
     *
     * @param ticketGrantingTicketId an SSO token identifying the requested authenticated Principal
     * @return valid Principal OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Principal getAuthenticatedPrincipalFrom(String ticketGrantingTicketId);

    /**
     * Retrieve a valid Principal's map of attributes identified by the provided TGT SSO token.
     *
     * @param ticketGrantingTicketId an SSO token identifying the requested authenticated Principal's attributes
     * @return valid Principal's attributes OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Map<String, Object> getPrincipalAttributesFrom(String ticketGrantingTicketId);

    /**
     * Update authentication associated with the ticket-granting ticket
     * and restore changes back to the registry; particularly updated authentication
     * attributes, etc.
     *
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @param authentication         the authentication
     */
    void updateAuthentication(String ticketGrantingTicketId, Authentication authentication);
}
