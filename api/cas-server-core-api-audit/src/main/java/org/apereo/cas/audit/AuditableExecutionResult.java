package org.apereo.cas.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link AuditableExecutionResult}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AuditableExecutionResult {
    private RegisteredService registeredService;
    private Service service;
    private ServiceTicket serviceTicket;
    private Authentication authentication;
    private RuntimeException exception;
    private TicketGrantingTicket ticketGrantingTicket;
    private AuthenticationResult authenticationResult;
    private Map<String, Object> properties = new TreeMap<>();

    public boolean isExecutionFailure() {
        return exception != null;
    }

    /**
     * Throw exception if needed.
     */
    public void throwExceptionIfNeeded() {
        if (isExecutionFailure()) {
            throw this.exception;
        }
    }

    /**
     * Factory method to create a result.
     *
     * @param e                 the exception
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final RuntimeException e, final Authentication authentication,
                                              final Service service, final RegisteredService registeredService) {
        final AuditableExecutionResult result = new AuditableExecutionResult();
        result.setAuthentication(authentication);
        result.setException(e);
        result.setRegisteredService(registeredService);
        result.setService(service);
        return result;
    }

    /**
     * Factory method to create a result.
     *
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final Authentication authentication,
                                              final Service service, final RegisteredService registeredService) {
        return of(null, authentication, service, registeredService);
    }

    /**
     * Of auditable execution result.
     *
     * @param serviceTicket        the service ticket
     * @param authenticationResult the authentication result
     * @param registeredService    the registered service
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final ServiceTicket serviceTicket, final AuthenticationResult authenticationResult,
                                              final RegisteredService registeredService) {
        final AuditableExecutionResult result = new AuditableExecutionResult();
        result.setServiceTicket(serviceTicket);
        result.setAuthenticationResult(authenticationResult);
        result.setRegisteredService(registeredService);
        return result;
    }

    /**
     * Of auditable execution result.
     *
     * @param service              the service
     * @param registeredService    the registered service
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final Service service, final RegisteredService registeredService, final TicketGrantingTicket ticketGrantingTicket) {
        final AuditableExecutionResult result = new AuditableExecutionResult();
        result.setTicketGrantingTicket(ticketGrantingTicket);
        result.setRegisteredService(registeredService);
        result.setService(service);
        return result;
    }
}
