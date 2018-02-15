package org.apereo.cas.audit;

import lombok.Builder;
import lombok.Getter;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AuditableContext}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Builder
public class AuditableContext {

    @Builder.Default
    private Optional<Service> service = Optional.empty();
    @Builder.Default
    private Optional<RegisteredService> registeredService= Optional.empty();
    @Builder.Default
    private Optional<Authentication> authentication= Optional.empty();
    @Builder.Default
    private Optional<ServiceTicket> serviceTicket= Optional.empty();
    @Builder.Default
    private Optional<AuthenticationResult> authenticationResult= Optional.empty();
    @Builder.Default
    private Optional<TicketGrantingTicket> ticketGrantingTicket= Optional.empty();
    @Builder.Default
    private Optional<Boolean> retrievePrincipalAttributesFromReleasePolicy= Optional.empty();
    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>();
}
