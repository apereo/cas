package org.apereo.cas.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
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
    @Setter
    private RuntimeException exception;

    /**
     * The execution result of the auditable action.
     */
    @Setter
    private Object executionResult;

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
    @Builder.Default
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
     * Of auditable execution result.
     *
     * @param context the context
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final AuditableContext context) {
        return AuditableExecutionResult.builder()
            .registeredService(context.getRegisteredService().orElseGet(null))
            .ticketGrantingTicket(context.getTicketGrantingTicket().orElseGet(null))
            .authentication(context.getAuthentication().orElseGet(null))
            .authenticationResult(context.getAuthenticationResult().orElseGet(null))
            .service(context.getService().orElseGet(null))
            .serviceTicket(context.getServiceTicket().orElseGet(null))
            .serviceTicket(context.getServiceTicket().orElseGet(null))
            .properties(context.getProperties())
            .build();
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
     * @return optional execution result.
     */
    public Optional<Object> getExecutionResult() {
        return Optional.ofNullable(executionResult);
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
