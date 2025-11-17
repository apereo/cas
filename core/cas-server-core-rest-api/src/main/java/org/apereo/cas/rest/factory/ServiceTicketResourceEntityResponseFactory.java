package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;

/**
 * This is {@link ServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface ServiceTicketResourceEntityResponseFactory extends Ordered {

    /**
     * Build response entity.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @param authenticationResult the authentication result
     * @return the response entity
     * @throws Throwable the throwable
     */
    ResponseEntity<String> build(String ticketGrantingTicket,
                                 WebApplicationService service,
                                 AuthenticationResult authenticationResult) throws Throwable;

    /**
     * Supports boolean.
     *
     * @param service              the service
     * @param authenticationResult the authentication result
     * @return true/false
     */
    boolean supports(WebApplicationService service, AuthenticationResult authenticationResult);
}
