package org.apereo.cas.support.rest;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This is {@link DefaultServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultServiceTicketResourceEntityResponseFactory implements ServiceTicketResourceEntityResponseFactory {
    private final CentralAuthenticationService centralAuthenticationService;

    public DefaultServiceTicketResourceEntityResponseFactory(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    public ResponseEntity<String> build(final String ticketGrantingTicket, final Service service,
                                        final AuthenticationResult authenticationResult) {
        final ServiceTicket serviceTicketId =
                centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        return new ResponseEntity<>(serviceTicketId.getId(), HttpStatus.OK);
    }
}
