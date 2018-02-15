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
import java.util.Optional;
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
    private Optional<RegisteredService> registeredService = Optional.empty();
    private Optional<Service> service = Optional.empty();
    private Optional<ServiceTicket> serviceTicket = Optional.empty();
    private Optional<Authentication> authentication = Optional.empty();
    private Optional<RuntimeException> exception = Optional.empty();
    private Optional<TicketGrantingTicket> ticketGrantingTicket = Optional.empty();
    private Optional<AuthenticationResult> authenticationResult = Optional.empty();

    private Map<String, Object> properties = new TreeMap<>();

    public boolean isExecutionFailure() {
        return exception.isPresent();
    }

    /**
     * Throw exception if needed.
     */
    public void throwExceptionIfNeeded() {
        if (isExecutionFailure()) {
            throw this.exception.get();
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
    public static AuditableExecutionResult of(final Optional<RuntimeException> e, final Authentication authentication,
                                              final Service service, final RegisteredService registeredService) {
        final AuditableExecutionResult result = new AuditableExecutionResult();
        result.setAuthentication(Optional.of(authentication));
        result.setException(e);
        result.setRegisteredService(Optional.of(registeredService));
        result.setService(Optional.of(service));
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
        return of(Optional.empty(), authentication, service, registeredService);
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
        result.setServiceTicket(Optional.of(serviceTicket));
        result.setAuthenticationResult(Optional.of(authenticationResult));
        result.setRegisteredService(Optional.of(registeredService));
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
        result.setTicketGrantingTicket(Optional.of(ticketGrantingTicket));
        result.setRegisteredService(Optional.of(registeredService));
        result.setService(Optional.of(service));
        return result;
    }

    /**
     * Of auditable execution result.
     *
     * @param context the context
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final AuditableContext context) {
        final AuditableExecutionResult result = new AuditableExecutionResult();
        context.getTicketGrantingTicket().ifPresent(obj -> result.setTicketGrantingTicket(Optional.of(obj)));
        context.getAuthentication().ifPresent(obj -> result.setAuthentication(Optional.of(obj)));
        context.getAuthenticationResult().ifPresent(obj -> result.setAuthenticationResult(Optional.of(obj)));
        context.getRegisteredService().ifPresent(obj -> result.setRegisteredService(Optional.of(obj)));
        context.getService().ifPresent(obj -> result.setService(Optional.of(obj)));
        context.getServiceTicket().ifPresent(obj -> result.setServiceTicket(Optional.of(obj)));
        result.getProperties().putAll(context.getProperties());
        return result;
    }
}
