package org.jasig.cas.web.flow.authentication;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyWebflowEventResolver}.
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

        if (service.getAuthenticationPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            logger.debug("Authentication policy does not contain any multifactor authentication providers");
            return null;
        }
        return resolveEventPerAuthenticationProvider(authentication.getPrincipal(), context, service);
    }
}
