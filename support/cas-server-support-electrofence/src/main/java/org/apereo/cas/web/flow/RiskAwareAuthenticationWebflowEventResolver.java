package org.apereo.cas.web.flow;

import org.apereo.cas.api.AuthenticationRiskEngine;
import org.apereo.cas.api.AuthenticationRiskMitigationEngine;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RiskAwareAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskAwareAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    private final AuthenticationRiskEngine authenticationRiskEngine;
    private final AuthenticationRiskMitigationEngine authenticationRiskMitigationEngine;

    public RiskAwareAuthenticationWebflowEventResolver(final AuthenticationRiskEngine authenticationRiskEngine, 
                                                       final AuthenticationRiskMitigationEngine authenticationRiskMitigationEngine) {
        this.authenticationRiskEngine = authenticationRiskEngine;
        this.authenticationRiskMitigationEngine = authenticationRiskMitigationEngine;
    }

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        final AuthenticationRiskScore score = authenticationRiskEngine.eval(authentication, service, request);
        authenticationRiskMitigationEngine.mitigate(authentication, service, score, request);
        
        return null;
    }
}
