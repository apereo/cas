package org.apereo.cas.audit;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.jspecify.annotations.Nullable;

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
@SuperBuilder
public class AuditableExecutionResult {

    /**
     * RegisteredService.
     */
    private @Nullable RegisteredService registeredService;

    /**
     * Service.
     */
    private @Nullable Service service;

    /**
     * ServiceTicket.
     */
    private @Nullable ServiceTicket serviceTicket;

    /**
     * Authentication.
     */
    private @Nullable Authentication authentication;

    /**
     * RuntimeException.
     */
    @Setter
    private @Nullable Throwable exception;

    /**
     * The execution result of the auditable action.
     */
    @Setter
    private @Nullable Object executionResult;

    /**
     * TicketGrantingTicket.
     */
    private @Nullable TicketGrantingTicket ticketGrantingTicket;

    /**
     * AuthenticationResult.
     */
    private @Nullable AuthenticationResult authenticationResult;

    /**
     * Properties.
     */
    @Builder.Default
    @Getter
    private Map<String, Object> properties = new TreeMap<>();

    /**
     * Of auditable execution result.
     *
     * @param context the context
     * @return the auditable execution result
     */
    public static AuditableExecutionResult of(final AuditableContext context) {
        val builder = AuditableExecutionResult.builder();
        context.getTicketGrantingTicket().ifPresent(builder::ticketGrantingTicket);
        context.getAuthentication().ifPresent(builder::authentication);
        context.getAuthenticationResult().ifPresent(builder::authenticationResult);
        context.getRegisteredService().ifPresent(builder::registeredService);
        context.getService().ifPresent(builder::service);
        context.getServiceTicket().ifPresent(builder::serviceTicket);
        builder.properties(context.getProperties());
        return builder.build();
    }

    public boolean isExecutionFailure() {
        return getException().isPresent();
    }

    /**
     * Throw exception if needed.
     */
    public void throwExceptionIfNeeded() throws Throwable {
        if (isExecutionFailure()) {
            throw getException().orElseThrow();
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
    public Optional<Throwable> getException() {
        return Optional.ofNullable(exception);
    }
}
