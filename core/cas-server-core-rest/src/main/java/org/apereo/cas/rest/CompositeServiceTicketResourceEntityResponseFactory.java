package org.apereo.cas.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

/**
 * This is {@link CompositeServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class CompositeServiceTicketResourceEntityResponseFactory implements ServiceTicketResourceEntityResponseFactory {
    private final Collection<ServiceTicketResourceEntityResponseFactory> chain;

    @Override
    public ResponseEntity<String> build(final String ticketGrantingTicket, final Service service, final AuthenticationResult authenticationResult) {
        final ServiceTicketResourceEntityResponseFactory factory = chain.stream()
            .filter(f -> f.supports(service, authenticationResult))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unable to locate a response entity factory to build a service ticket. "
                + "This generally is due to a configuration issue where CAS is unable to recognize the incoming request"));
        return factory.build(ticketGrantingTicket, service, authenticationResult);
    }

    @Override
    public boolean supports(final Service service, final AuthenticationResult authenticationResult) {
        return service != null && authenticationResult != null;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
