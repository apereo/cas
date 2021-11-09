package org.apereo.cas.audit;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Builder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AuditableContext}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Builder
public class AuditableContext {

    private final Service service;

    private final RegisteredService registeredService;

    private final Principal principal;

    private final Authentication authentication;

    private final ServiceTicket serviceTicket;

    private final AuthenticationResult authenticationResult;

    private final TicketGrantingTicket ticketGrantingTicket;

    private final Object httpRequest;

    private final Object httpResponse;

    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>(0);

    public Optional<Service> getService() {
        return Optional.ofNullable(service);
    }

    public Optional<RegisteredService> getRegisteredService() {
        return Optional.ofNullable(registeredService);
    }

    public Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(authentication);
    }

    public Optional<Principal> getPrincipal() {
        return Optional.ofNullable(this.principal);
    }

    public Optional<ServiceTicket> getServiceTicket() {
        return Optional.ofNullable(serviceTicket);
    }

    public Optional<Object> getRequest() {
        return Optional.ofNullable(httpRequest);
    }

    public Optional<Object> getResponse() {
        return Optional.ofNullable(httpResponse);
    }

    public Optional<AuthenticationResult> getAuthenticationResult() {
        return Optional.ofNullable(authenticationResult);
    }

    public Optional<TicketGrantingTicket> getTicketGrantingTicket() {
        return Optional.ofNullable(ticketGrantingTicket);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
