package org.apereo.cas.services;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;

import java.util.Map;

/**
 * This is {@link RegisteredServiceAccessStrategyAuditableEnforcer}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegisteredServiceAccessStrategyAuditableEnforcer extends BaseAuditableExecution {
    @Override
    @Audit(action = AuditableActions.SERVICE_ACCESS_ENFORCEMENT,
        actionResolverName = AuditActionResolvers.SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER)
    public AuditableExecutionResult execute(final AuditableContext context) {
        val providedRegisteredService = context.getRegisteredService();
        if (context.getServiceTicket().isPresent() && context.getAuthenticationResult().isPresent() && providedRegisteredService.isPresent()) {
            val result = AuditableExecutionResult.of(context);
            try {
                val serviceTicket = context.getServiceTicket().orElseThrow();
                val authResult = context.getAuthenticationResult().orElseThrow().getAuthentication();
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(serviceTicket.getService(),
                    providedRegisteredService.get(), authResult.getPrincipal().getId(),
                    (Map) CollectionUtils.merge(authResult.getAttributes(), authResult.getPrincipal().getAttributes()));
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
                val authResult = ticketGrantingTicket.get().getRoot().getAuthentication();
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService, authResult.getPrincipal().getId(),
                    (Map) CollectionUtils.merge(authResult.getAttributes(), authResult.getPrincipal().getAttributes()));
            } catch (final PrincipalException | UnauthorizedServiceException e) {
                result.setException(e);
            }
            return result;
        }


        val providedPrincipal = context.getPrincipal();
        if (providedService.isPresent() && providedRegisteredService.isPresent() && providedPrincipal.isPresent()) {
            val registeredService = providedRegisteredService.get();
            val service = providedService.get();
            val principal = providedPrincipal.get();

            val result = AuditableExecutionResult.builder()
                .registeredService(registeredService)
                .service(service)
                .build();

            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService, principal.getId(), principal.getAttributes());
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
                    registeredService, authentication.getPrincipal().getId(),
                    (Map) CollectionUtils.merge(authentication.getAttributes(), authentication.getPrincipal().getAttributes()));
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
