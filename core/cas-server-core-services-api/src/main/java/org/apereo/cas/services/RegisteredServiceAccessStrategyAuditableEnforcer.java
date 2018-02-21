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

        if (context.getServiceTicket().isPresent() && context.getAuthenticationResult().isPresent() && context.getRegisteredService().isPresent()) {
            final AuditableExecutionResult result = AuditableExecutionResult.of(context);
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(context.getServiceTicket().get(),
                    context.getAuthenticationResult().get(), context.getRegisteredService().get());
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        if (context.getService().isPresent() && context.getRegisteredService().isPresent() && context.getTicketGrantingTicket().isPresent()) {
            final AuditableExecutionResult result = AuditableExecutionResult.of(context.getService().get(),
                context.getRegisteredService().get(), context.getTicketGrantingTicket().get());
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(context.getService().get(),
                    context.getRegisteredService().get(),
                    context.getTicketGrantingTicket().get(),
                    context.getRetrievePrincipalAttributesFromReleasePolicy().orElse(Boolean.TRUE));
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }

        if (context.getService().isPresent() && context.getRegisteredService().isPresent() && context.getAuthentication().isPresent()) {
            final AuditableExecutionResult result = AuditableExecutionResult.of(context.getAuthentication().get(), context.getService().get(), context.getRegisteredService().get());
            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(context.getService().get(),
                    context.getRegisteredService().get(),
                    context.getAuthentication().get(),
                    context.getRetrievePrincipalAttributesFromReleasePolicy().orElse(Boolean.TRUE));
            } catch (final PrincipalException e) {
                result.setException(e);
            }
            return result;
        }
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service unauthorized");
    }
}
