package org.apereo.cas.oidc.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link OidcPushedAuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OidcPushedAuthorizationRequest extends Ticket, AuthenticationAwareTicket, TicketState {
    /**
     * Ticket prefix.
     */
    String PREFIX = "OPAR";

    /**
     * Gets service.
     *
     * @return the service
     */
    Service getService();

    /**
     * Gets registered service.
     *
     * @return the registered service
     */
    OAuthRegisteredService getRegisteredService();

    /**
     * Gets authorization request.
     *
     * @return the authorization request
     */
    String getAuthorizationRequest();
}
