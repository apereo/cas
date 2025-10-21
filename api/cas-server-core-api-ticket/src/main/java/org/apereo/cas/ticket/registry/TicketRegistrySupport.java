package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

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
     * Default bean name.
     */
    String BEAN_NAME = "defaultTicketRegistrySupport";

    /**
     * Retrieve a valid Authentication object identified by the provided TGT SSO token.
     *
     * @param ticketGrantingTicketId an SSO token identifying the requested Authentication
     * @return valid Authentication OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Authentication getAuthenticationFrom(String ticketGrantingTicketId);

    /**
     * Retrieve a valid tgt object identified by the provided TGT SSO token.
     *
     * @param ticketGrantingTicketId an SSO token identifying the requested Authentication
     * @return valid TGT OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    TicketGrantingTicket getTicketGrantingTicket(String ticketGrantingTicketId);

    /**
     * Retrieve a valid ticket object identified by the provided the id and transform it into a ticket state.
     *
     * @param ticketId the ticket id
     * @return the ticket state
     */
    Ticket getTicket(String ticketId);

    /**
     * Retrieve a valid Principal object identified by the provided TGT SSO token.
     *
     * @param ticketGrantingTicketId an SSO token identifying the requested authenticated Principal
     * @return valid Principal OR <b>NULL</b> if there is no valid SSO session present identified by the provided TGT id SSO token
     */
    Principal getAuthenticatedPrincipalFrom(String ticketGrantingTicketId);
    
    /**
     * Update authentication associated with the ticket-granting ticket
     * and restore changes back to the registry; particularly updated authentication
     * attributes, etc.
     *
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @param authentication         the authentication
     * @throws Exception the exception
     */
    void updateAuthentication(String ticketGrantingTicketId, Authentication authentication) throws Exception;

    /**
     * Gets ticket registry.
     *
     * @return the ticket registry
     */
    TicketRegistry getTicketRegistry();
}
