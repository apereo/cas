package org.apereo.cas.support.rest.factory;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.springframework.http.ResponseEntity;

/**
 * This is {@link ServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface ServiceTicketResourceEntityResponseFactory {

    /**
     * Build response response entity.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @param authenticationResult the authentication result
     * @return the response entity
     */
    ResponseEntity<String> build(String ticketGrantingTicket,
                                 Service service,
                                 AuthenticationResult authenticationResult);
}
