package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link OidcPushedAuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OidcPushedAuthorizationRequest extends AuthenticationAwareTicket {
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
