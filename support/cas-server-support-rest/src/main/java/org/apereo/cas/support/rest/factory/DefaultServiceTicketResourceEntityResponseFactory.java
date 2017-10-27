package org.apereo.cas.support.rest.factory;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This is {@link DefaultServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultServiceTicketResourceEntityResponseFactory implements ServiceTicketResourceEntityResponseFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceTicketResourceEntityResponseFactory.class);
    
    /**
     * The central authentication service implementation.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    public DefaultServiceTicketResourceEntityResponseFactory(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    public ResponseEntity<String> build(final String ticketGrantingTicket, final Service service,
                                        final AuthenticationResult authenticationResult) {
        final String serviceTicketId = grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        return new ResponseEntity<>(serviceTicketId, HttpStatus.OK);
    }

    /**
     * Grant service ticket service ticket.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @param authenticationResult the authentication result
     * @return the service ticket
     */
    protected String grantServiceTicket(final String ticketGrantingTicket, final Service service, final AuthenticationResult authenticationResult) {
        final ServiceTicket ticket = centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);

        LOGGER.debug("Generated service ticket [{}]", ticket.getId());
        return ticket.getId();
    }
}
