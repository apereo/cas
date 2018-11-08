package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

/**
 * This is {@link CompositeServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class CompositeServiceTicketResourceEntityResponseFactory implements ServiceTicketResourceEntityResponseFactory {
    private final Collection<ServiceTicketResourceEntityResponseFactory> chain;

    @Audit(
        action = "REST_API_SERVICE_TICKET",
        actionResolverName = "REST_API_SERVICE_TICKET_ACTION_RESOLVER",
        resourceResolverName = "REST_API_SERVICE_TICKET_RESOURCE_RESOLVER")
    @Override
    public ResponseEntity<String> build(final String ticketGrantingTicket, final Service service, final AuthenticationResult authenticationResult) {
        val factory = chain.stream()
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
