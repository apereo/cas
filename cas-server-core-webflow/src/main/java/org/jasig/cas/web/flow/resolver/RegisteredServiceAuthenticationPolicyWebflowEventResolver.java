package org.jasig.cas.web.flow.resolver;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAuthenticationPolicy;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyWebflowEventResolver}
 * that attempts to resolve the next event basef on the authentication providers of this service.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("registeredServiceAuthenticationPolicyWebflowEventResolver")
public class RegisteredServiceAuthenticationPolicyWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final RegisteredServiceAuthenticationPolicy policy = service.getAuthenticationPolicy();
        if (policy == null || policy.getMultifactorAuthenticationProviders().isEmpty()) {
            logger.debug("Authentication policy does not contain any multifactor authentication providers");
            return null;
        }
        return resolveEventPerAuthenticationProvider(authentication.getPrincipal(), context, service);
    }
}
