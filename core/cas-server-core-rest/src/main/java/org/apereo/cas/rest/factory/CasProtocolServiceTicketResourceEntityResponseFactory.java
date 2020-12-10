package org.apereo.cas.rest.factory;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.WebApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This is {@link CasProtocolServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasProtocolServiceTicketResourceEntityResponseFactory implements ServiceTicketResourceEntityResponseFactory {
    /**
     * The central authentication service implementation.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    @Override
    public ResponseEntity<String> build(final String ticketGrantingTicket, final WebApplicationService webApplicationService,
                                        final AuthenticationResult authenticationResult) {
        val serviceTicketId = grantServiceTicket(ticketGrantingTicket, webApplicationService, authenticationResult);
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
    protected String grantServiceTicket(final String ticketGrantingTicket, final WebApplicationService service,
                                        final AuthenticationResult authenticationResult) {
        val ticket = centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);

        LOGGER.debug("Generated service ticket [{}]", ticket.getId());
        return ticket.getId();
    }

    @Override
    public boolean supports(final WebApplicationService service, final AuthenticationResult authenticationResult) {
        return service instanceof SimpleWebApplicationServiceImpl;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
