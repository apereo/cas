package org.apereo.cas.audit;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
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

    private final Authentication authentication;

    private final ServiceTicket serviceTicket;

    private final AuthenticationResult authenticationResult;

    private final TicketGrantingTicket ticketGrantingTicket;

    private final Object httpRequest;

    private final Object httpResponse;

    /**
     * Should attributes be refreshed from the release policy?
     */
    @Builder.Default
    private Boolean retrievePrincipalAttributesFromReleasePolicy = Boolean.FALSE;

    /**
     * Properties.
     */
    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>(0);

    /**
     * Get service.
     *
     * @return optional service
     */
    public Optional<Service> getService() {
        return Optional.ofNullable(service);
    }

    /**
     * Get registered service.
     *
     * @return optional registered service
     */
    public Optional<RegisteredService> getRegisteredService() {
        return Optional.ofNullable(registeredService);
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
     * @return optional service ticket
     */
    public Optional<ServiceTicket> getServiceTicket() {
        return Optional.ofNullable(serviceTicket);
    }

    public Optional<Object> getRequest() {
        return Optional.ofNullable(httpRequest);
    }

    public Optional<Object> getResponse() {
        return Optional.ofNullable(httpResponse);
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
     * @return optional tgt
     */
    public Optional<TicketGrantingTicket> getTicketGrantingTicket() {
        return Optional.ofNullable(ticketGrantingTicket);
    }

    /**
     * Get.
     *
     * @return optional attribute retrieval policy flag
     */
    public Optional<Boolean> getRetrievePrincipalAttributesFromReleasePolicy() {
        return Optional.ofNullable(retrievePrincipalAttributesFromReleasePolicy);
    }

    /**
     * Get.
     *
     * @return optional properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
}
