package org.apereo.cas.audit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

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

    private Optional<Service> service;
    private Optional<RegisteredService> registeredService;
    private Optional<Authentication> authentication;
    private Optional<ServiceTicket> serviceTicket;
    private Optional<AuthenticationResult> authenticationResult;
    private Optional<TicketGrantingTicket> ticketGrantingTicket;
    private Optional<Boolean> retrievePrincipalAttributesFromReleasePolicy;
}
