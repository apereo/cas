package org.apereo.cas.services.support;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.inspektr.audit.annotation.Audit;

/**
 * Default implementation of {@link RegisteredServiceAccessStrategyEnforcer}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class DefaultRegisteredServiceAccessStrategyEnforcer implements RegisteredServiceAccessStrategyEnforcer {

    @Override
    @Audit(action = "SERVICE_ACCESS_ENFORCEMENT", actionResolverName = "SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER",
            resourceResolverName = "SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER")
    public ServiceAccessCheckResult enforceServiceAccessStrategy(final Service service,
                                                                 final RegisteredService registeredService,
                                                                 final Authentication authentication,
                                                                 final boolean retrievePrincipalAttributesFromReleasePolicy) {

        try {
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                    registeredService, authentication, retrievePrincipalAttributesFromReleasePolicy);
        } catch (final PrincipalException e) {
            return createResult(e, authentication, service, registeredService);
        }

        return createResult(null, authentication, service, registeredService);
    }

    private ServiceAccessCheckResult createResult(final PrincipalException pe,
                                                  final Authentication authentication,
                                                  final Service service,
                                                  final RegisteredService registeredService) {

        return new ServiceAccessCheckResult(pe,
                authentication.getPrincipal().getId(),
                authentication.getPrincipal().getAttributes(),
                service.getId(),
                registeredService);
    }
}
