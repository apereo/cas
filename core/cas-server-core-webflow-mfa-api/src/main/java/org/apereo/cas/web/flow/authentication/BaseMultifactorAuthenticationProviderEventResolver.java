package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseMultifactorAuthenticationProviderEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class BaseMultifactorAuthenticationProviderEventResolver extends AbstractCasMultifactorAuthenticationWebflowEventResolver {

    public BaseMultifactorAuthenticationProviderEventResolver(
        final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext) {
        super(webflowEventResolutionConfigurationContext);
    }

    /**
     * Resolve registered service in request context.
     *
     * @param requestContext the request context
     * @return the registered service
     */
    protected RegisteredService resolveRegisteredServiceInRequestContext(final RequestContext requestContext) {
        val resolvedService = resolveServiceFromAuthenticationRequest(requestContext);
        if (resolvedService != null) {
            val service = getWebflowEventResolutionConfigurationContext().getServicesManager().findServiceBy(resolvedService);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(resolvedService, service);
            return service;
        }
        LOGGER.debug("Authentication request is not accompanied by a service given none is specified");
        return null;
    }
}
