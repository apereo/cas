package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This is {@link SamlRestServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class SamlRestServiceTicketResourceEntityResponseFactory implements ServiceTicketResourceEntityResponseFactory {
    private final UniqueTicketIdGenerator uniqueTicketIdGenerator;

    @Override
    public ResponseEntity<String> build(final String ticketGrantingTicket, final Service service, final AuthenticationResult authenticationResult) {
        val serviceTicketId = uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX);
        return new ResponseEntity<>(serviceTicketId, HttpStatus.OK);
    }

    @Override
    public boolean supports(final Service service, final AuthenticationResult authenticationResult) {
        return service instanceof SamlService && authenticationResult != null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
