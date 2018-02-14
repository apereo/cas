package org.apereo.cas.services;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.inspektr.audit.annotation.Audit;

/**
 * This is {@link RegisteredServiceAccessStrategyAuditableEnforcer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegisteredServiceAccessStrategyAuditableEnforcer extends BaseAuditableExecution {
    @Override
    @Audit(action = "SERVICE_ACCESS_ENFORCEMENT",
        actionResolverName = "SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER",
        resourceResolverName = "SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER")
    public AuditableExecutionResult execute(final Object... parameters) {
        final Service service = getParameter(parameters, 0, Service.class);
        final Authentication authentication = getParameter(parameters, 1, Authentication.class);
        final RegisteredService registeredService = getParameter(parameters, 2, RegisteredService.class);
        final Boolean retrievePrincipalAttributesFromReleasePolicy = getParameter(parameters, 3, Boolean.class);
        try {
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                registeredService, authentication, retrievePrincipalAttributesFromReleasePolicy);
        } catch (final PrincipalException e) {
            return AuditableExecutionResult.of(e, authentication, service, registeredService);
        }
        return AuditableExecutionResult.of(authentication, service, registeredService);
    }
}
