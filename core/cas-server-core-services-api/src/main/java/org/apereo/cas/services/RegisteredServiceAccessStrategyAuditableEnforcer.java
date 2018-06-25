package org.apereo.cas.services;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.inspektr.audit.annotation.Audit;

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
    public AuditableExecutionResult execute(final AuditableContext context) {
        final var registeredService = context.getRegisteredService();
        if (context.getServiceTicket().isPresent() && context.getAuthenticationResult().isPresent() && registeredService.isPresent()) {
            final var result = AuditableExecutionResult.of(context);
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(context.getServiceTicket().get(),
                    context.getAuthenticationResult().get(), registeredService.get());
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        final var service = context.getService();
        final var ticketGrantingTicket = context.getTicketGrantingTicket();
        if (service.isPresent() && registeredService.isPresent() && ticketGrantingTicket.isPresent()) {
            final var result = AuditableExecutionResult.of(service.get(),
                registeredService.get(), ticketGrantingTicket.get());
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service.get(),
                    registeredService.get(),
                    ticketGrantingTicket.get(),
                    context.getRetrievePrincipalAttributesFromReleasePolicy().orElse(Boolean.TRUE));
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        final var authentication = context.getAuthentication();
        if (service.isPresent() && registeredService.isPresent() && authentication.isPresent()) {
            final var result = AuditableExecutionResult.of(authentication.get(), service.get(), registeredService.get());
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service.get(),
                    registeredService.get(),
                    authentication.get(),
                    context.getRetrievePrincipalAttributesFromReleasePolicy().orElse(Boolean.TRUE));
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        if (service.isPresent() && registeredService.isPresent()) {
            final var result = AuditableExecutionResult.of(service.get(), registeredService.get());
            try {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service.get(), registeredService.get());
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        if (registeredService.isPresent()) {
            final var result = AuditableExecutionResult.of(registeredService.get());
            try {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService.get());
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service unauthorized");
    }
}
