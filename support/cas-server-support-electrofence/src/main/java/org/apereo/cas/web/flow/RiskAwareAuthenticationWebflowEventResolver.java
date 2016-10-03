package org.apereo.cas.web.flow;

import org.apereo.cas.api.AuthenticationRiskEngine;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RiskAwareAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskAwareAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    private final AuthenticationRiskEngine authenticationRiskEngine;

    public RiskAwareAuthenticationWebflowEventResolver(final AuthenticationRiskEngine authenticationRiskEngine) {
        this.authenticationRiskEngine = authenticationRiskEngine;
    }

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }
        
        return null;
    }
}
