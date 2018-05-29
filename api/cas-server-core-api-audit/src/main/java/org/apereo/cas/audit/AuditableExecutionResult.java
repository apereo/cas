package org.apereo.cas.audit;

import lombok.AllArgsConstructor;
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
public class AuditableExecutionResult {

    /**
     * RegisteredService.
     */
    private RegisteredService registeredService;

    /**
     * Service.
     */
    private Service service;

    /**
     * ServiceTicket.
     */
    private ServiceTicket serviceTicket;

    /**
     * Authentication.
     */
    private Authentication authentication;

    /**
     * RuntimeException.
     */
    private RuntimeException exception;

    /**
     * TicketGrantingTicket.
     */
    private TicketGrantingTicket ticketGrantingTicket;

    /**
     * AuthenticationResult.
     */
    private AuthenticationResult authenticationResult;

    /**
     * Properties.
     */
    private Map<String, Object> properties = new TreeMap<>();

    public boolean isExecutionFailure() {
        return getException().isPresent();
    }

    /**
     * Throw exception if needed.
     */
    public void throwExceptionIfNeeded() {
        if (isExecutionFailure()) {
            throw getException().get();
        }
    }

    /**
     * Add property.
     *
     * @param name  the name
     * @param value the value
     */
    public void addProperty(final String name, final Object value) {
        this.properties.put(name, value);
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
        final var result = new AuditableExecutionResult();
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
        final var result = new AuditableExecutionResult();
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
        final var result = new AuditableExecutionResult();
        result.setTicketGrantingTicket(ticketGrantingTicket);
        result.setRegisteredService(registeredService);
        result.setService(service);
        return result;
    }

    /**
     * Of auditable execution result.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final Service service, final RegisteredService registeredService) {
        final var result = new AuditableExecutionResult();
        result.setRegisteredService(registeredService);
        result.setService(service);
        return result;
    }

    /**
     * Of auditable execution result.
     *
     * @param registeredService the registered service
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final RegisteredService registeredService) {
        final var result = new AuditableExecutionResult();
        result.setRegisteredService(registeredService);
        return result;
    }

    /**
     * Of auditable execution result.
     *
     * @param context the context
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final AuditableContext context) {
        final var result = new AuditableExecutionResult();
        context.getTicketGrantingTicket().ifPresent(result::setTicketGrantingTicket);
        context.getAuthentication().ifPresent(result::setAuthentication);
        context.getAuthenticationResult().ifPresent(result::setAuthenticationResult);
        context.getRegisteredService().ifPresent(result::setRegisteredService);
        context.getService().ifPresent(result::setService);
        context.getServiceTicket().ifPresent(result::setServiceTicket);
        result.getProperties().putAll(context.getProperties());
        return result;
    }

    /**
     * Get.
     *
     * @return optional registered service
     */
    public Optional<RegisteredService> getRegisteredService() {
        return Optional.ofNullable(registeredService);
    }

    /**
     * Get.
     *
     * @return optional service
     */
    public Optional<Service> getService() {
        return Optional.ofNullable(service);
    }

    /**
     * Get.
     *
     * @return optional service ticket
     */
    public Optional<ServiceTicket> getServiceTicket() {
        return Optional.ofNullable(serviceTicket);
    }

    /**
     * Get.
     *
     * @return optional authentication
     */
    public Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(authentication);
    }

    /**
     * Get.
     *
     * @return optional tgt
     */
    public Optional<TicketGrantingTicket> getTicketGrantingTicket() {
        return Optional.ofNullable(ticketGrantingTicket);
    }

    /**
     * Get.
     *
     * @return optional authentication result
     */
    public Optional<AuthenticationResult> getAuthenticationResult() {
        return Optional.ofNullable(authenticationResult);
    }

    /**
     * Get.
     *
     * @return optional exception
     */
    public Optional<RuntimeException> getException() {
        return Optional.ofNullable(exception);
    }

    /**
     * Get.
     *
     * @return properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
}
