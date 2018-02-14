package org.apereo.cas.services;

import org.apache.commons.lang3.ObjectUtils;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.inspektr.audit.annotation.Audit;

import java.util.Objects;

/**
 * This is {@link RegisteredServiceAccessStrategyAuditableEnforcer}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegisteredServiceAccessStrategyAuditableEnforcer extends BaseAuditableExecution {
    @Override
    @Audit(action = "SERVICE_ACCESS_ENFORCEMENT",
        actionResolverName = "SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER",
        resourceResolverName = "SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER")
    public AuditableExecutionResult execute(final Object... parameters) {
        
        final Service service = getUniqueParameter(parameters, Service.class);
        final Authentication authentication = getUniqueParameter(parameters, Authentication.class);
        final RegisteredService registeredService = getUniqueParameter(parameters, RegisteredService.class);
        final Boolean retrievePrincipalAttributesFromReleasePolicy = getUniqueParameter(parameters, Boolean.class);
        final TicketGrantingTicket ticketGrantingTicket = getUniqueParameter(parameters, TicketGrantingTicket.class);
        final ProxyGrantingTicket proxyGrantingTicket = getUniqueParameter(parameters, ProxyGrantingTicket.class);
        final ServiceTicket serviceTicket = getUniqueParameter(parameters, ServiceTicket.class);
        final AuthenticationResult authenticationResult = getUniqueParameter(parameters, AuthenticationResult.class);

        if (serviceTicket != null && authenticationResult != null && registeredService != null) {
            final AuditableExecutionResult result = AuditableExecutionResult.of(serviceTicket, authenticationResult, registeredService);
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(serviceTicket, authenticationResult, registeredService);
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        if (service != null && registeredService != null && (ticketGrantingTicket != null || proxyGrantingTicket != null)) {
            final TicketGrantingTicket ticket = ObjectUtils.defaultIfNull(ticketGrantingTicket, proxyGrantingTicket);
            final AuditableExecutionResult result = AuditableExecutionResult.of(service, registeredService, ticket);
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService,
                    ticket, retrievePrincipalAttributesFromReleasePolicy);
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        Objects.requireNonNull(service, "service cannot be null");
        Objects.requireNonNull(service, "registeredService cannot be null");
        Objects.requireNonNull(service, "authentication cannot be null");
        
        try {
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                registeredService, authentication, retrievePrincipalAttributesFromReleasePolicy);
        } catch (final PrincipalException e) {
            return AuditableExecutionResult.of(e, authentication, service, registeredService);
        }
        return AuditableExecutionResult.of(authentication, service, registeredService);
    }
}
