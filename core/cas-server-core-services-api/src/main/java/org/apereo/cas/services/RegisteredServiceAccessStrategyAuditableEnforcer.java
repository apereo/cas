package org.apereo.cas.services;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.authentication.PrincipalException;

import lombok.val;
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
        val providedRegisteredService = context.getRegisteredService();
        if (context.getServiceTicket().isPresent() && context.getAuthenticationResult().isPresent() && providedRegisteredService.isPresent()) {
            val result = AuditableExecutionResult.of(context);
            try {
                val serviceTicket = context.getServiceTicket().orElseThrow();
                val authResult = context.getAuthenticationResult().orElseThrow();
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(serviceTicket,
                    authResult, providedRegisteredService.get());
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return result;
        }

        val providedService = context.getService();
        val ticketGrantingTicket = context.getTicketGrantingTicket();
        if (providedService.isPresent() && providedRegisteredService.isPresent() && ticketGrantingTicket.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();
            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .ticketGrantingTicket(ticketGrantingTicket.get())
                .build();
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService,
                    ticketGrantingTicket.get(),
                    context.getRetrievePrincipalAttributesFromReleasePolicy().orElse(Boolean.TRUE));
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return result;
        }

        val providedAuthn = context.getAuthentication();
        if (providedService.isPresent() && providedRegisteredService.isPresent() && providedAuthn.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();
            val authentication = providedAuthn.get();

            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .authentication(authentication)
                .build();

            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService,
                    authentication,
                    context.getRetrievePrincipalAttributesFromReleasePolicy().orElse(Boolean.TRUE));
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return result;
        }

        if (providedService.isPresent() && providedRegisteredService.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();

            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .build();
            try {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return result;
        }

        if (providedRegisteredService.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .build();
            try {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return result;
        }

        val result = AuditableExecutionResult.builder().build();
        result.setException(new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service unauthorized"));
        return result;
    }
}
